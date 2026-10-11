package com.carddemo.interest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Monthly interest calculation, migrated from COBOL batch program CBACT04C (JCL job INTCALC).
 * Business rules are referenced as BR-n; see modern/README.md.
 */
public final class InterestCalculator {

    static final String DEFAULT_GROUP = "DEFAULT";
    private static final BigDecimal MONTHS_TIMES_PERCENT = BigDecimal.valueOf(1200);
    private static final DateTimeFormatter DB2_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS");

    /** Controls how far the migrated code mirrors the legacy program. */
    public enum Mode {
        /** Bit-for-bit parity with CBACT04C, including legacy defect LD-1. */
        LEGACY_PARITY,
        /** Same results, but LD-1 fixed: the last account in the file is also updated. */
        CORRECTED
    }

    public record Result(List<InterestTransaction> transactions, List<Account> accounts) {}

    private final Map<String, Account> accountsById;
    private final Map<String, CardXref> xrefByAccountId;
    private final Map<String, DisclosureGroup> disclosureGroupsByKey;
    private final Clock clock;
    private final Mode mode;

    public InterestCalculator(Map<String, Account> accountsById,
                              Map<String, CardXref> xrefByAccountId,
                              Map<String, DisclosureGroup> disclosureGroupsByKey,
                              Clock clock,
                              Mode mode) {
        this.accountsById = accountsById;
        this.xrefByAccountId = xrefByAccountId;
        this.disclosureGroupsByKey = disclosureGroupsByKey;
        this.clock = clock;
        this.mode = mode;
    }

    /** BR-1: balances are processed in key order (account, type, category); interest is accumulated per account. */
    public Result run(List<TransactionCategoryBalance> balances, String parmDate) {
        List<InterestTransaction> transactions = new ArrayList<>();
        Account current = null;
        CardXref currentXref = null;
        BigDecimal totalInterest = BigDecimal.ZERO;
        int tranIdSuffix = 0;

        for (TransactionCategoryBalance balance : balances) {
            if (current == null || !current.id().equals(balance.accountId())) {
                if (current != null) {
                    current.postInterest(totalInterest);
                }
                totalInterest = BigDecimal.ZERO;
                current = require(accountsById.get(balance.accountId()), "ACCOUNT NOT FOUND: " + balance.accountId());
                currentXref = require(xrefByAccountId.get(balance.accountId()), "ACCOUNT NOT FOUND: " + balance.accountId());
            }

            BigDecimal rate = interestRate(current.groupId(), balance.typeCode(), balance.categoryCode());
            if (rate.signum() != 0) {
                BigDecimal monthlyInterest = monthlyInterest(balance.balance(), rate);
                totalInterest = totalInterest.add(monthlyInterest);
                tranIdSuffix++;
                transactions.add(new InterestTransaction(
                        transactionId(parmDate, tranIdSuffix), current.id(), currentXref.cardNumber(),
                        monthlyInterest, timestamp()));
            }
        }

        // LD-1: CBACT04C leaves its PERFORM UNTIL loop before the final 1050-UPDATE-ACCOUNT, so the last
        // account's interest is written as a transaction but never added to its balance.
        if (current != null && mode == Mode.CORRECTED) {
            current.postInterest(totalInterest);
        }
        return new Result(transactions, List.copyOf(accountsById.values()));
    }

    /** BR-2: monthly interest = balance x annual rate / 1200, truncated (not rounded) to 2 decimals. */
    static BigDecimal monthlyInterest(BigDecimal balance, BigDecimal annualRatePercent) {
        return balance.multiply(annualRatePercent).divide(MONTHS_TIMES_PERCENT, 2, RoundingMode.DOWN);
    }

    /** BR-3: rate comes from the account's disclosure group; BR-4: fall back to group DEFAULT if missing. */
    BigDecimal interestRate(String groupId, String typeCode, String categoryCode) {
        DisclosureGroup group = disclosureGroupsByKey.get(DisclosureGroup.key(groupId, typeCode, categoryCode));
        if (group == null) {
            group = require(disclosureGroupsByKey.get(DisclosureGroup.key(DEFAULT_GROUP, typeCode, categoryCode)),
                    "ERROR READING DEFAULT DISCLOSURE GROUP");
        }
        return group.interestRate();
    }

    /** BR-6: transaction id = 10-char JCL PARM date + 6-digit run-wide sequence. */
    static String transactionId(String parmDate, int suffix) {
        return FixedWidth.pad(parmDate, 10) + FixedWidth.unsigned(suffix, 6);
    }

    private String timestamp() {
        return LocalDateTime.now(clock).format(DB2_TIMESTAMP) + "0000";
    }

    private static <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalStateException(message);
        }
        return Objects.requireNonNull(value);
    }
}
