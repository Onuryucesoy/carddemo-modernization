package com.carddemo.interest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterestRulesTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2022-07-18T10:15:30.120Z"), ZoneOffset.UTC);

    @Test
    @DisplayName("BR-2: monthly interest = balance x rate / 1200")
    void monthlyInterestFormula() {
        assertEquals(new BigDecimal("12.50"), InterestCalculator.monthlyInterest(bd("1000.00"), bd("15.00")));
    }

    @Test
    @DisplayName("BR-2: result is truncated, never rounded (COMPUTE without ROUNDED)")
    void truncatesInsteadOfRounding() {
        assertEquals(new BigDecimal("6.94"), InterestCalculator.monthlyInterest(bd("333.33"), bd("25.00")));
        assertEquals(new BigDecimal("0.00"), InterestCalculator.monthlyInterest(bd("0.07"), bd("25.00")));
    }

    @Test
    @DisplayName("BR-2: negative (credit) balances truncate toward zero")
    void negativeBalancesTruncateTowardZero() {
        assertEquals(new BigDecimal("-10.41"), InterestCalculator.monthlyInterest(bd("-500.00"), bd("25.00")));
    }

    @Test
    @DisplayName("BR-4: missing disclosure group falls back to DEFAULT")
    void fallsBackToDefaultGroup() {
        InterestCalculator calc = calculator(Map.of(), Mode.LEGACY);
        assertEquals(new BigDecimal("15.00"), calc.interestRate("PREMIUM", "01", "0001"));
    }

    @Test
    @DisplayName("BR-4: abends when the DEFAULT group is missing too")
    void failsWithoutDefaultGroup() {
        InterestCalculator calc = new InterestCalculator(Map.of(), Map.of(), Map.of(), CLOCK,
                InterestCalculator.Mode.LEGACY_PARITY);
        assertThrows(IllegalStateException.class, () -> calc.interestRate("PREMIUM", "01", "0001"));
    }

    @Test
    @DisplayName("BR-5: zero-rate categories produce no transaction but the account is still updated")
    void zeroRateProducesNoTransaction() {
        Map<String, Account> accounts = accounts("ZEROAPR", "A000000000");
        InterestCalculator.Result result = calculator(accounts, Mode.LEGACY).run(List.of(
                bal("00000000001", "1000.00"), bal("00000000002", "1000.00")), "2022071800");

        assertEquals(1, result.transactions().size());
        Account zeroApr = accounts.get("00000000001");
        assertEquals(new BigDecimal("1000.00"), zeroApr.currentBalance());
        assertEquals(new BigDecimal("0.00"), zeroApr.cycleCredit());
    }

    @Test
    @DisplayName("BR-6/BR-7: transaction id, codes, description and card number")
    void transactionRecordLayout() {
        Map<String, Account> accounts = accounts("A000000000");
        InterestTransaction tx = calculator(accounts, Mode.LEGACY)
                .run(List.of(bal("00000000001", "1000.00")), "2022071800").transactions().get(0);

        assertEquals("2022071800000001", tx.id());
        assertEquals("Int. for a/c 00000000001", tx.description());
        assertEquals("4000000000000001", tx.cardNumber());
        String record = tx.toRecord();
        assertEquals(350, record.length());
        assertEquals("010005System    ", record.substring(16, 32));
        assertEquals("2022-07-18-10.15.30.120000", record.substring(278, 304));
    }

    @Test
    @DisplayName("LD-1: legacy parity leaves the LAST account's balance unposted")
    void legacyDefectLastAccountNotPosted() {
        Map<String, Account> accounts = accounts("A000000000", "A000000000");
        InterestCalculator.Result result = calculator(accounts, Mode.LEGACY).run(List.of(
                bal("00000000001", "1000.00"), bal("00000000002", "1000.00")), "2022071800");

        assertEquals(2, result.transactions().size(), "interest transaction is still written");
        assertEquals(new BigDecimal("1012.50"), accounts.get("00000000001").currentBalance());
        assertEquals(new BigDecimal("1000.00"), accounts.get("00000000002").currentBalance());
    }

    @Test
    @DisplayName("LD-1: corrected mode posts interest to the last account as well")
    void correctedModePostsLastAccount() {
        Map<String, Account> accounts = accounts("A000000000", "A000000000");
        calculator(accounts, Mode.CORRECTED).run(List.of(
                bal("00000000001", "1000.00"), bal("00000000002", "1000.00")), "2022071800");

        assertEquals(new BigDecimal("1012.50"), accounts.get("00000000002").currentBalance());
    }

    private enum Mode { LEGACY, CORRECTED }

    private static InterestCalculator calculator(Map<String, Account> accounts, Mode mode) {
        Map<String, CardXref> xref = new TreeMap<>();
        for (String id : accounts.keySet()) {
            xref.put(id, new CardXref("40000000000000" + id.substring(9), "000000100", id));
        }
        Map<String, DisclosureGroup> groups = new TreeMap<>();
        for (DisclosureGroup g : List.of(
                new DisclosureGroup("A000000000", "01", "0001", bd("15.00")),
                new DisclosureGroup("DEFAULT", "01", "0001", bd("15.00")),
                new DisclosureGroup("ZEROAPR", "01", "0001", bd("0.00")))) {
            groups.put(g.key(), g);
        }
        return new InterestCalculator(accounts, xref, groups, CLOCK,
                mode == Mode.LEGACY ? InterestCalculator.Mode.LEGACY_PARITY : InterestCalculator.Mode.CORRECTED);
    }

    private static Map<String, Account> accounts(String... groups) {
        Map<String, Account> accounts = new TreeMap<>();
        for (int i = 0; i < groups.length; i++) {
            String id = String.format("%011d", i + 1);
            String record = id + "Y" + ZonedDecimal.format(bd("1000.00"), 10, 2)
                    + "0".repeat(24) + "2020-01-012030-01-012030-01-01"
                    + ZonedDecimal.format(bd("10.00"), 10, 2) + ZonedDecimal.format(bd("20.00"), 10, 2)
                    + "60601     " + FixedWidth.pad(groups[i], 10);
            accounts.put(id, Account.parse(record));
        }
        return accounts;
    }

    private static TransactionCategoryBalance bal(String accountId, String amount) {
        return new TransactionCategoryBalance(accountId, "01", "0001", bd(amount));
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}
