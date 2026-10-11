package com.carddemo.interest;

import java.math.BigDecimal;

/** Copybook CVTRA01Y (TRAN-CAT-BAL-RECORD, 50 bytes). */
public record TransactionCategoryBalance(String accountId, String typeCode, String categoryCode, BigDecimal balance) {

    public static TransactionCategoryBalance parse(String line) {
        String r = FixedWidth.pad(line, 50);
        return new TransactionCategoryBalance(
                r.substring(0, 11), r.substring(11, 13), r.substring(13, 17),
                ZonedDecimal.parse(r.substring(17, 28), 2));
    }
}
