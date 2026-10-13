package com.carddemo.interest;

/** Card cross-reference record, copybook CVACT03Y (50 bytes). */
public record CardXref(String record) {
    public static final int LENGTH = 50;

    public CardXref {
        record = Records.fixed(record, LENGTH);
    }

    public String cardNumber() { return record.substring(0, 16); }
    public String accountId() { return record.substring(25, 36); }
}
