package com.carddemo.interest;

import java.math.BigDecimal;

/** Account master record, copybook CVACT01Y (300 bytes). Unchanged bytes are preserved as-is. */
public final class Account {
    public static final int LENGTH = 300;
    private static final int CURR_BAL = 12;
    private static final int CYC_CREDIT = 78;
    private static final int CYC_DEBIT = 90;
    private static final int AMOUNT_LEN = 12;

    private final StringBuilder record;

    public Account(String record) {
        this.record = new StringBuilder(Records.fixed(record, LENGTH));
    }

    public String id() { return record.substring(0, 11); }
    public String groupId() { return record.substring(112, 122); }
    public BigDecimal currentBalance() { return amount(CURR_BAL); }
    public BigDecimal cycleCredit() { return amount(CYC_CREDIT); }
    public BigDecimal cycleDebit() { return amount(CYC_DEBIT); }
    public String record() { return record.toString(); }

    /** Paragraph 1050-UPDATE-ACCOUNT. */
    void postInterest(BigDecimal totalInterest) {
        setAmount(CURR_BAL, currentBalance().add(totalInterest));
        setAmount(CYC_CREDIT, BigDecimal.ZERO);
        setAmount(CYC_DEBIT, BigDecimal.ZERO);
    }

    private BigDecimal amount(int offset) {
        return ZonedDecimal.parse(record.substring(offset, offset + AMOUNT_LEN), 2);
    }

    private void setAmount(int offset, BigDecimal value) {
        record.replace(offset, offset + AMOUNT_LEN, ZonedDecimal.format(value, 10, 2));
    }
}
