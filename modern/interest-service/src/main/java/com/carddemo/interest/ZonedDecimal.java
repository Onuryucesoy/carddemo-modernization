package com.carddemo.interest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** COBOL zoned decimal ({@code PIC S9(n)V9(m)} DISPLAY) with an overpunched trailing sign. */
public final class ZonedDecimal {
    private static final String POSITIVE = "{ABCDEFGHI";
    private static final String NEGATIVE = "}JKLMNOPQR";

    private ZonedDecimal() {}

    public static BigDecimal parse(CharSequence field, int scale) {
        String s = field.toString();
        char last = s.charAt(s.length() - 1);
        boolean negative = NEGATIVE.indexOf(last) >= 0;
        int lastDigit = negative ? NEGATIVE.indexOf(last)
                : POSITIVE.indexOf(last) >= 0 ? POSITIVE.indexOf(last) : Character.digit(last, 10);
        if (lastDigit < 0) {
            throw new IllegalArgumentException("not a zoned decimal: [" + s + "]");
        }
        BigInteger unscaled = new BigInteger(s.substring(0, s.length() - 1) + lastDigit);
        BigDecimal value = new BigDecimal(unscaled, scale);
        return negative ? value.negate() : value;
    }

    public static String format(BigDecimal value, int intDigits, int scale) {
        BigDecimal v = fit(value, intDigits, scale);
        String digits = String.format("%0" + (intDigits + scale) + "d", v.unscaledValue().abs());
        int last = digits.charAt(digits.length() - 1) - '0';
        char sign = (v.signum() < 0 ? NEGATIVE : POSITIVE).charAt(last);
        return digits.substring(0, digits.length() - 1) + sign;
    }

    /**
     * Stores a value the way COBOL does into {@code PIC S9(intDigits)V9(scale)} when no
     * ROUNDED / ON SIZE ERROR is coded: extra decimals and high-order integer digits are dropped.
     */
    public static BigDecimal fit(BigDecimal value, int intDigits, int scale) {
        BigDecimal truncated = value.setScale(scale, RoundingMode.DOWN);
        BigInteger limit = BigInteger.TEN.pow(intDigits + scale);
        BigInteger magnitude = truncated.unscaledValue().abs().mod(limit);
        BigInteger unscaled = truncated.signum() < 0 ? magnitude.negate() : magnitude;
        return new BigDecimal(unscaled, scale);
    }
}
