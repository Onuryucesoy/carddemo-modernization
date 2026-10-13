package com.carddemo.interest;

import java.math.BigDecimal;

/** TCATBAL record, copybook CVTRA01Y (50 bytes). */
public record TranCatBalance(String record) {
    public static final int LENGTH = 50;

    public TranCatBalance {
        record = Records.fixed(record, LENGTH);
    }

    public String key() { return record.substring(0, 17); }
    public String accountId() { return record.substring(0, 11); }
    public String typeCode() { return record.substring(11, 13); }
    public String categoryCode() { return record.substring(13, 17); }
    public BigDecimal balance() { return ZonedDecimal.parse(record.substring(17, 28), 2); }
}
