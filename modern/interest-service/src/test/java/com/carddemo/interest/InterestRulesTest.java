package com.carddemo.interest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Business rules of CBACT04C, each one pinned by a small focused example. */
class InterestRulesTest {

    static final LocalDateTime RUN_AT = LocalDateTime.of(2022, 7, 18, 23, 30, 0, 120_000_000);

    @ParameterizedTest(name = "{0} x {1}% / 1200 = {2}")
    @CsvSource({
            "1234.56, 15.00, 15.43",
            "789.01, 25.00, 16.43",
            "-833.33, 15.00, -10.41",
            "99.99, 25.00, 2.08",
            "0.01, 25.00, 0.00",
            "10000.00, 15.00, 125.00",
    })
    void monthlyInterestIsBalanceTimesAprOver1200TruncatedToCents(String balance, String rate, String expected) {
        assertEquals(new BigDecimal(expected), InterestCalculator.monthlyInterest(new BigDecimal(balance), new BigDecimal(rate)));
    }

    @Test
    void monthlyInterestKeepsOnlyNineIntegerDigits() {
        assertEquals(new BigDecimal("333324999.91"),
                InterestCalculator.monthlyInterest(new BigDecimal("999999999.99"), new BigDecimal("9999.99")));
    }

    @Test
    void postsSummedInterestAndResetsCycleTotals() {
        BatchResult r = run(
                List.of(tcat(11, "01", 1, "1234.56"), tcat(11, "01", 2, "789.01"), tcat(99, "01", 1, "0")),
                List.of(account(11, "2523.57", "100.00", "250.00", "A000000000"), account(99, "0", "0", "0", "A000000000")),
                List.of(disc("A000000000", "01", 1, "15.00"), disc("A000000000", "01", 2, "25.00")));
        Account a = r.account(11);
        assertEquals(new BigDecimal("2555.43"), a.currentBalance());
        assertEquals(new BigDecimal("0.00"), a.cycleCredit());
        assertEquals(new BigDecimal("0.00"), a.cycleDebit());
        assertEquals(2, r.transactions().size());
    }

    @Test
    void missingGroupFallsBackToDefaultRateForSameCategory() {
        BatchResult r = run(
                List.of(tcat(12, "01", 1, "-833.33"), tcat(99, "01", 1, "0")),
                List.of(account(12, "0", "0", "0", "GOLDCARD"), account(99, "0", "0", "0", "DEFAULT")),
                List.of(disc("DEFAULT", "01", 1, "15.00")));
        assertEquals(new BigDecimal("-10.41"), r.transactions().get(0).amount());
        assertTrue(r.sysout().containsAll(List.of("DISCLOSURE GROUP RECORD MISSING", "TRY WITH DEFAULT GROUP CODE")));
    }

    @Test
    void zeroRateWritesNoTransactionButStillRewritesAccount() {
        BatchResult r = run(
                List.of(tcat(13, "01", 1, "5000.00"), tcat(99, "01", 1, "0")),
                List.of(account(13, "5000.00", "0", "5000.00", "ZEROAPR"), account(99, "0", "0", "0", "ZEROAPR")),
                List.of(disc("ZEROAPR", "01", 1, "0")));
        assertTrue(r.transactions().isEmpty());
        assertEquals(new BigDecimal("0.00"), r.account(13).cycleDebit());
    }

    @Test
    void interestTransactionRecordLayout() {
        BatchResult r = run(
                List.of(tcat(11, "01", 1, "1234.56")),
                List.of(account(11, "0", "0", "0", "A000000000")),
                List.of(disc("A000000000", "01", 1, "15.00")));
        InterestTransaction t = r.transactions().get(0);
        assertEquals("2022071800000001", t.id());
        assertEquals("4111111111110011", t.cardNumber());
        String rec = t.record();
        assertEquals(350, rec.length());
        assertEquals("2022071800000001" + "01" + "0005" + "System    " + "Int. for a/c 00000000011", rec.substring(0, 56));
        assertEquals("0000000154C", rec.substring(132, 143));
        assertEquals("4111111111110011", rec.substring(262, 278));
        assertEquals("2022-07-18-23.30.00.120000", rec.substring(278, 304));
        assertEquals("2022-07-18-23.30.00.120000", rec.substring(304, 330));
    }

    /**
     * Legacy defect, preserved on purpose for parity: the PERFORM UNTIL loop exits on
     * end-of-file before the final 1050-UPDATE-ACCOUNT, so the last account's interest
     * transaction is written but its balance is never posted.
     */
    @Test
    void lastAccountInFileIsNotPosted() {
        BatchResult r = run(
                List.of(tcat(15, "01", 1, "2000.00")),
                List.of(account(15, "2000.00", "0", "2000.00", "A000000000")),
                List.of(disc("A000000000", "01", 1, "15.00")));
        assertEquals(new BigDecimal("25.00"), r.transactions().get(0).amount());
        assertEquals(new BigDecimal("2000.00"), r.account(15).currentBalance());
        assertEquals(new BigDecimal("2000.00"), r.account(15).cycleDebit());
    }

    @Test
    void missingDefaultRateAbendsWithU0999() {
        BatchResult r = run(
                List.of(tcat(22, "09", 9, "50.00")),
                List.of(account(22, "0", "0", "0", "")),
                List.of(disc("DEFAULT", "01", 1, "15.00")));
        assertEquals("ABEND U0999", r.result());
        assertEquals(List.of("ERROR READING DEFAULT DISCLOSURE GROUP", "FILE STATUS IS: NNNN0023", "ABENDING PROGRAM"),
                r.sysout().subList(r.sysout().size() - 3, r.sysout().size()));
    }

    @Test
    void accountsWithoutBalancesAreUntouched() {
        String untouched = account(16, "42.00", "1.00", "2.00", "A000000000");
        BatchResult r = run(
                List.of(tcat(11, "01", 1, "1.00"), tcat(99, "01", 1, "0")),
                List.of(account(11, "0", "0", "0", "A000000000"), untouched, account(99, "0", "0", "0", "A000000000")),
                List.of(disc("A000000000", "01", 1, "15.00")));
        assertEquals(untouched, r.account(16).record());
    }

    // ---- fixture builders (same layouts as golden-master/fixtures/make_fixtures.py) ----

    static BatchResult run(List<String> tcat, List<String> accounts, List<String> discgrp) {
        List<String> xref = accounts.stream()
                .map(a -> "41111111111100" + a.substring(9, 11) + "0000001" + a.substring(9, 11) + a.substring(0, 11))
                .toList();
        BatchInput in = BatchInput.fromRecords(tcat, xref, accounts, discgrp);
        return new InterestBatch("2022071800", RUN_AT).run(in);
    }

    static String tcat(long acct, String type, int cat, String bal) {
        return String.format("%011d%s%04d%s", acct, type, cat, ZonedDecimal.format(new BigDecimal(bal), 9, 2)) + "0".repeat(22);
    }

    static String account(long acct, String bal, String cycCredit, String cycDebit, String group) {
        String rec = String.format("%011dY%s%s%s2019-01-152027-01-152025-01-15%s%s%-10s%-10s", acct,
                z12(bal), z12("5000"), z12("1500"), z12(cycCredit), z12(cycDebit), "10001", group);
        return String.format("%-300s", rec);
    }

    static String disc(String group, String type, int cat, String rate) {
        return String.format("%-10s%s%04d%s", group, type, cat, ZonedDecimal.format(new BigDecimal(rate), 4, 2)) + "0".repeat(28);
    }

    private static String z12(String v) {
        return ZonedDecimal.format(new BigDecimal(v), 10, 2);
    }
}
