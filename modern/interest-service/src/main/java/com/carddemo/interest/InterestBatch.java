package com.carddemo.interest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Java port of batch program CBACT04C (job INTCALC): computes monthly interest per
 * transaction-category balance, writes one interest transaction per rated category and
 * posts the total to the account. Behaviour, including legacy quirks, matches the COBOL.
 */
public final class InterestBatch {
    static final String DEFAULT_GROUP = "DEFAULT";
    static final int ABEND_CODE = 999;
    private static final String FILE_STATUS_NOT_FOUND = "FILE STATUS IS: NNNN0023";

    private final String parmDate;
    private final String timestamp;

    /**
     * @param parmDate     the job PARM (first 10 characters become the transaction-id prefix)
     * @param runTimestamp clock value used for TRAN-ORIG-TS / TRAN-PROC-TS
     */
    public InterestBatch(String parmDate, LocalDateTime runTimestamp) {
        this.parmDate = String.format("%-10.10s", parmDate);
        this.timestamp = db2Timestamp(runTimestamp);
    }

    public BatchResult run(BatchInput input) {
        List<String> sysout = new ArrayList<>();
        List<InterestTransaction> transactions = new ArrayList<>();
        sysout.add("START OF EXECUTION OF PROGRAM CBACT04C");
        String result = "RC=0000";
        try {
            String lastAccountId = null;
            Account account = null;
            CardXref xref = null;
            BigDecimal totalInterest = BigDecimal.ZERO;
            int tranIdSuffix = 0;

            for (TranCatBalance balance : input.balances) {
                sysout.add(balance.record());
                if (!balance.accountId().equals(lastAccountId)) {
                    if (account != null) {
                        account.postInterest(totalInterest);
                    }
                    totalInterest = BigDecimal.ZERO;
                    lastAccountId = balance.accountId();
                    account = input.accounts.get(lastAccountId);
                    if (account == null) {
                        sysout.add("ACCOUNT NOT FOUND: " + lastAccountId);
                        throw abend(sysout, "ERROR READING ACCOUNT FILE");
                    }
                    xref = input.xrefByAccount.get(lastAccountId);
                    if (xref == null) {
                        sysout.add("ACCOUNT NOT FOUND: " + lastAccountId);
                        throw abend(sysout, "ERROR READING XREF FILE");
                    }
                }
                BigDecimal rate = interestRate(input, account.groupId(), balance, sysout);
                if (rate.signum() != 0) {
                    BigDecimal monthly = InterestCalculator.monthlyInterest(balance.balance(), rate);
                    totalInterest = ZonedDecimal.fit(totalInterest.add(monthly), 9, 2);
                    tranIdSuffix = (tranIdSuffix + 1) % 1_000_000;
                    transactions.add(new InterestTransaction(parmDate + String.format("%06d", tranIdSuffix),
                            account.id(), xref.cardNumber(), monthly, timestamp));
                }
            }
            // Legacy behaviour kept for parity: the COBOL loop ends at end-of-file before its
            // final 1050-UPDATE-ACCOUNT, so the last account's total is never posted.
            sysout.add("END OF EXECUTION OF PROGRAM CBACT04C");
        } catch (Abend e) {
            result = String.format("ABEND U%04d", ABEND_CODE);
        }
        return new BatchResult(sysout, transactions, List.copyOf(input.accounts.values()), result);
    }

    /** Paragraphs 1200-GET-INTEREST-RATE and 1200-A-GET-DEFAULT-INT-RATE. */
    private static BigDecimal interestRate(BatchInput input, String groupId, TranCatBalance balance, List<String> sysout) {
        DisclosureGroup group = input.disclosureGroups.get(
                DisclosureGroup.key(groupId, balance.typeCode(), balance.categoryCode()));
        if (group == null) {
            sysout.add("DISCLOSURE GROUP RECORD MISSING");
            sysout.add("TRY WITH DEFAULT GROUP CODE");
            group = input.disclosureGroups.get(
                    DisclosureGroup.key(DEFAULT_GROUP, balance.typeCode(), balance.categoryCode()));
            if (group == null) {
                throw abend(sysout, "ERROR READING DEFAULT DISCLOSURE GROUP");
            }
        }
        return group.interestRate();
    }

    private static Abend abend(List<String> sysout, String message) {
        sysout.add(message);
        sysout.add(FILE_STATUS_NOT_FOUND);
        sysout.add("ABENDING PROGRAM");
        return new Abend();
    }

    /** Z-GET-DB2-FORMAT-TIMESTAMP: YYYY-MM-DD-HH.MM.SS.hh0000 (hundredths of a second). */
    static String db2Timestamp(LocalDateTime t) {
        return String.format("%04d-%02d-%02d-%02d.%02d.%02d.%02d0000", t.getYear(), t.getMonthValue(),
                t.getDayOfMonth(), t.getHour(), t.getMinute(), t.getSecond(), t.getNano() / 10_000_000);
    }

    private static final class Abend extends RuntimeException {
        Abend() {
            super(null, null, false, false);
        }
    }
}
