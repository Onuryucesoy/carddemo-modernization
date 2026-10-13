package com.carddemo.interest;

/** Copybook CVACT03Y (CARD-XREF-RECORD, 50 bytes). */
public record CardXref(String cardNumber, String customerId, String accountId) {

    public static CardXref parse(String line) {
        String r = FixedWidth.pad(line, 50);
        return new CardXref(r.substring(0, 16), r.substring(16, 25), r.substring(25, 36));
    }
}
