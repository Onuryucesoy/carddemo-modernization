package com.carddemo.interest;

import java.math.BigDecimal;

/** Copybook CVTRA02Y (DIS-GROUP-RECORD, 50 bytes). Interest rate is an annual percentage, PIC S9(04)V99. */
public record DisclosureGroup(String groupId, String typeCode, String categoryCode, BigDecimal interestRate) {

    public static DisclosureGroup parse(String line) {
        String r = FixedWidth.pad(line, 50);
        return new DisclosureGroup(r.substring(0, 10), r.substring(10, 12), r.substring(12, 16),
                ZonedDecimal.parse(r.substring(16, 22), 2));
    }

    public String key() {
        return key(groupId, typeCode, categoryCode);
    }

    public static String key(String groupId, String typeCode, String categoryCode) {
        return FixedWidth.pad(groupId, 10) + typeCode + categoryCode;
    }
}
