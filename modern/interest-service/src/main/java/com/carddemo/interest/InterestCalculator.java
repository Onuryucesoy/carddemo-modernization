package com.carddemo.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Paragraph 1300-COMPUTE-INTEREST: {@code WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200}. */
public final class InterestCalculator {
    private static final BigDecimal MONTHS_TIMES_PERCENT = BigDecimal.valueOf(1200);

    private InterestCalculator() {}

    /** Truncated (not rounded) to cents and stored in PIC S9(09)V99, as the COBOL COMPUTE does. */
    public static BigDecimal monthlyInterest(BigDecimal balance, BigDecimal annualRatePercent) {
        BigDecimal exact = balance.multiply(annualRatePercent).divide(MONTHS_TIMES_PERCENT, 2, RoundingMode.DOWN);
        return ZonedDecimal.fit(exact, 9, 2);
    }
}
