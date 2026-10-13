package com.carddemo.interest;

import java.math.BigDecimal;

/** Disclosure group (interest rate) record, copybook CVTRA02Y (50 bytes). */
public record DisclosureGroup(String record) {
    public static final int LENGTH = 50;

    public DisclosureGroup {
        record = Records.fixed(record, LENGTH);
    }

    /** Group id X(10) + transaction type X(2) + category 9(4). */
    public String key() { return record.substring(0, 16); }
    public BigDecimal interestRate() { return ZonedDecimal.parse(record.substring(16, 22), 2); }

    static String key(String groupId, String typeCode, String categoryCode) {
        return String.format("%-10.10s", groupId) + typeCode + categoryCode;
    }
}
