package com.carddemo.batch;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Sequential reader over one fixed-length COBOL record. Each call consumes the next field in
 * copybook order, so a mapper reads like the copybook it implements.
 *
 * <p>Exception messages carry only the field offset, never record content: records contain
 * card numbers, SSNs and passwords.
 */
public final class FixedRecord {

    private static final DateTimeFormatter COBOL_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private final byte[] bytes;
    private final Charset charset;
    private int pos;

    private FixedRecord(byte[] bytes, Charset charset) {
        this.bytes = bytes;
        this.charset = charset;
    }

    public static FixedRecord of(byte[] bytes, Charset charset) {
        return new FixedRecord(bytes, charset);
    }

    public int length() {
        return bytes.length;
    }

    public int position() {
        return pos;
    }

    /** {@code X(n)}, decoded as-is. */
    public String raw(int len) {
        return new String(take(len), charset);
    }

    /** {@code X(n)} with trailing spaces and LOW-VALUES removed; all-blank becomes {@code null}. */
    public String text(int len) {
        String s = stripTrailing(raw(len));
        return s.isEmpty() ? null : s;
    }

    /** {@code X(01)} flag; blank becomes {@code null}. */
    public Character flag() {
        String s = text(1);
        return s == null ? null : s.charAt(0);
    }

    /** {@code FILLER}. */
    public FixedRecord skip(int len) {
        take(len);
        return this;
    }

    /** {@code 9(n)} unsigned zoned decimal. All-blank becomes {@code null}. */
    public Long unsigned(int digits) {
        BigDecimal v = zoned(digits, 0);
        return v == null ? null : v.longValueExact();
    }

    public Integer unsignedInt(int digits) {
        Long v = unsigned(digits);
        return v == null ? null : Math.toIntExact(v);
    }

    /**
     * {@code S9(i)V9(s)} zoned decimal with the sign overpunched on the last digit
     * ({@code {}=+0, A-I=+1..9, }=-0, J-R=-1..9}); a plain last digit is positive.
     * {@code digits} is the total digit count (integer + fraction).
     */
    public BigDecimal zoned(int digits, int scale) {
        int start = pos;
        String s = raw(digits);
        if (stripTrailing(s).isEmpty()) {
            return null;
        }
        char last = s.charAt(digits - 1);
        boolean negative = false;
        char lastDigit;
        int p;
        if ((p = POSITIVE_OVERPUNCH.indexOf(last)) >= 0) {
            lastDigit = (char) ('0' + p);
        } else if ((p = NEGATIVE_OVERPUNCH.indexOf(last)) >= 0) {
            lastDigit = (char) ('0' + p);
            negative = true;
        } else {
            lastDigit = last;
        }
        String number = s.substring(0, digits - 1) + lastDigit;
        for (int i = 0; i < number.length(); i++) {
            if (number.charAt(i) < '0' || number.charAt(i) > '9') {
                throw invalid("zoned decimal", start, digits);
            }
        }
        BigInteger unscaled = new BigInteger(number);
        return new BigDecimal(negative ? unscaled.negate() : unscaled, scale);
    }

    /** {@code S9(i)V9(s) COMP-3}: two digits per byte, sign in the low nibble of the last byte. */
    public BigDecimal packed(int digits, int scale) {
        int len = digits / 2 + 1;
        int start = pos;
        byte[] b = take(len);
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) {
            int hi = (b[i] >> 4) & 0x0F;
            int lo = b[i] & 0x0F;
            sb.append(nibble(hi, start, len));
            if (i < len - 1) {
                sb.append(nibble(lo, start, len));
            } else if (lo < 0x0A) {
                throw invalid("packed decimal sign", start, len);
            } else if (lo == 0x0D || lo == 0x0B) {
                sb.insert(0, '-');
            }
        }
        return new BigDecimal(new BigInteger(sb.toString()), scale);
    }

    /** {@code 9(n) COMP}: big-endian binary of 2, 4 or 8 bytes depending on {@code n}. */
    public long binary(int digits) {
        int len = digits <= 4 ? 2 : digits <= 9 ? 4 : 8;
        long v = 0;
        byte[] b = take(len);
        for (byte x : b) {
            v = (v << 8) | (x & 0xFF);
        }
        int shift = 64 - len * 8;
        return (v << shift) >> shift;
    }

    /** {@code X(10)} date in {@code YYYY-MM-DD} form; blank becomes {@code null}. */
    public LocalDate date() {
        int start = pos;
        String s = text(10);
        try {
            return s == null ? null : LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw invalid("date", start, 10);
        }
    }

    /** {@code X(26)} timestamp in {@code YYYY-MM-DD HH:MM:SS.NNNNNN} form; blank becomes {@code null}. */
    public LocalDateTime timestamp() {
        int start = pos;
        String s = text(26);
        try {
            return s == null ? null : LocalDateTime.parse(s, COBOL_TIMESTAMP);
        } catch (DateTimeParseException e) {
            throw invalid("timestamp", start, 26);
        }
    }

    /** Fails if the mapper did not consume exactly the whole record. */
    public void assertFullyConsumed(String layout) {
        if (pos != bytes.length) {
            throw new IllegalStateException(
                    layout + " consumed " + pos + " bytes but the record is " + bytes.length + " bytes");
        }
    }

    private byte[] take(int len) {
        if (pos + len > bytes.length) {
            throw new IllegalArgumentException(
                    "Record too short: field at offset " + pos + " needs " + len + " bytes, record has " + bytes.length);
        }
        byte[] out = new byte[len];
        System.arraycopy(bytes, pos, out, 0, len);
        pos += len;
        return out;
    }

    private static char nibble(int n, int start, int len) {
        if (n > 9) {
            throw invalid("packed decimal digit", start, len);
        }
        return (char) ('0' + n);
    }

    private static IllegalArgumentException invalid(String what, int offset, int len) {
        return new IllegalArgumentException("Invalid " + what + " at offset " + offset + " (length " + len + ")");
    }

    private static String stripTrailing(String s) {
        int end = s.length();
        while (end > 0 && (s.charAt(end - 1) == ' ' || s.charAt(end - 1) == '\u0000')) {
            end--;
        }
        return s.substring(0, end);
    }
}
