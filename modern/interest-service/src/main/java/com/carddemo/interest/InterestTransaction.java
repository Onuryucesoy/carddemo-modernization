package com.carddemo.interest;

import java.math.BigDecimal;

/** Copybook CVTRA05Y (TRAN-RECORD, 350 bytes) as written by paragraph 1300-B-WRITE-TX. */
public record InterestTransaction(String id, String accountId, String cardNumber, BigDecimal amount, String timestamp) {

    public static final String TYPE_CODE = "01";
    public static final String CATEGORY_CODE = "0005";
    public static final String SOURCE = "System";

    public String description() {
        return "Int. for a/c " + accountId;
    }

    public String toRecord() {
        return FixedWidth.pad(id, 16)
                + TYPE_CODE
                + CATEGORY_CODE
                + FixedWidth.pad(SOURCE, 10)
                + FixedWidth.pad(description(), 100)
                + ZonedDecimal.format(amount, 9, 2)
                + FixedWidth.unsigned(0, 9)
                + FixedWidth.pad("", 50)
                + FixedWidth.pad("", 50)
                + FixedWidth.pad("", 10)
                + FixedWidth.pad(cardNumber, 16)
                + FixedWidth.pad(timestamp, 26)
                + FixedWidth.pad(timestamp, 26)
                + FixedWidth.pad("", 20);
    }
}
