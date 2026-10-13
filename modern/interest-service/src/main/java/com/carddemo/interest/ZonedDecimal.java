package com.carddemo.interest;

import java.math.BigDecimal;
import java.math.BigInteger;

/** Codec for COBOL signed zoned decimal (PIC S9(n)V9(s) DISPLAY, EBCDIC-style overpunch). */
public final class ZonedDecimal {
    private static final String POSITIVE = "{ABCDEFGHI";
    private static final String NEGATIVE = "}JKLMNOPQR";

    private ZonedDecimal() {}

    public static BigDecimal parse(String field, int scale) {
        char last = field.charAt(field.length() - 1);
        String digits = field.substring(0, field.length() - 1);
        boolean negative = false;
        int lastDigit;
        if (POSITIVE.indexOf(last) >= 0) {
            lastDigit = POSITIVE.indexOf(last);
        } else if (NEGATIVE.indexOf(last) >= 0) {
            lastDigit = NEGATIVE.indexOf(last);
            negative = true;
        } else if (Character.isDigit(last)) {
            lastDigit = last - '0';
        } else {
            throw new IllegalArgumentException("Invalid zoned decimal: '" + field + "'");
        }
        BigInteger unscaled = new BigInteger(digits.replace(' ', '0') + lastDigit);
        return new BigDecimal(negative ? unscaled.negate() : unscaled, scale);
    }

    /** Formats like a COBOL MOVE: truncates excess integer digits on the left and fraction digits on the right. */
    public static String format(BigDecimal value, int integerDigits, int scale) {
        int length = integerDigits + scale;
        BigInteger unscaled = value.setScale(scale, java.math.RoundingMode.DOWN).unscaledValue();
        String digits = unscaled.abs().toString();
        if (digits.length() > length) {
            digits = digits.substring(digits.length() - length);
        }
        digits = "0".repeat(length - digits.length()) + digits;
        int last = digits.charAt(length - 1) - '0';
        char sign = unscaled.signum() < 0 ? NEGATIVE.charAt(last) : POSITIVE.charAt(last);
        return digits.substring(0, length - 1) + sign;
    }
}
