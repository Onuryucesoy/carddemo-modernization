package com.carddemo.interest;

import java.math.BigDecimal;

/**
 * Copybook CVACT01Y (ACCOUNT-RECORD, 300 bytes). Keeps the raw record so that fields this program does not
 * touch are rewritten byte-for-byte, as the COBOL REWRITE does.
 */
public final class Account {
    private static final int CURR_BAL = 12;
    private static final int CYC_CREDIT = 78;
    private static final int CYC_DEBIT = 90;
    private static final int GROUP_ID = 112;

    private final StringBuilder raw;

    private Account(String raw) {
        this.raw = new StringBuilder(FixedWidth.pad(raw, 300));
    }

    public static Account parse(String line) {
        return new Account(line);
    }

    public String id() {
        return raw.substring(0, 11);
    }

    public String groupId() {
        return raw.substring(GROUP_ID, GROUP_ID + 10);
    }

    public BigDecimal currentBalance() {
        return ZonedDecimal.parse(raw.substring(CURR_BAL, CURR_BAL + 12), 2);
    }

    public BigDecimal cycleCredit() {
        return ZonedDecimal.parse(raw.substring(CYC_CREDIT, CYC_CREDIT + 12), 2);
    }

    public BigDecimal cycleDebit() {
        return ZonedDecimal.parse(raw.substring(CYC_DEBIT, CYC_DEBIT + 12), 2);
    }

    /** Paragraph 1050-UPDATE-ACCOUNT: add the month's interest and reset the cycle totals. */
    void postInterest(BigDecimal totalInterest) {
        set(CURR_BAL, ZonedDecimal.format(currentBalance().add(totalInterest), 10, 2));
        set(CYC_CREDIT, ZonedDecimal.format(BigDecimal.ZERO, 10, 2));
        set(CYC_DEBIT, ZonedDecimal.format(BigDecimal.ZERO, 10, 2));
    }

    private void set(int offset, String value) {
        raw.replace(offset, offset + value.length(), value);
    }

    public String toRecord() {
        return raw.toString();
    }
}
