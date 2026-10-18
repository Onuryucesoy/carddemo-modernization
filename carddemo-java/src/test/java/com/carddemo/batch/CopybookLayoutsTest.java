package com.carddemo.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.batch.CopybookLayouts.Layout;
import com.carddemo.batch.FixedLengthRecordReader.Encoding;
import com.carddemo.config.ImportProperties.Source;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategory;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.entity.TransactionType;
import com.carddemo.entity.User;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Parses the real sample files in app/data and checks them field by field. */
class CopybookLayoutsTest {

    private final SampleDataFiles files =
            new SampleDataFiles(Path.of("../app/data"), Source.ASCII, Charset.forName("IBM037"));

    private <T> List<T> ascii(SampleDataset d, Layout<T> l) {
        return files.readAll(d, l, Encoding.ASCII);
    }

    private <T> List<T> ebcdic(SampleDataset d, Layout<T> l) {
        return files.readAll(d, l, Encoding.EBCDIC);
    }

    @Test
    void accountRecord() {
        Account a = ascii(SampleDataset.ACCTDATA, CopybookLayouts.ACCOUNT).get(0);
        assertThat(a.getAcctId()).isEqualTo(1L);
        assertThat(a.getActiveStatus()).isEqualTo('Y');
        assertThat(a.getCurrBal()).isEqualByComparingTo("194.00");
        assertThat(a.getCreditLimit()).isEqualByComparingTo("2020.00");
        assertThat(a.getCashCreditLimit()).isEqualByComparingTo("1020.00");
        assertThat(a.getOpenDate()).isEqualTo(LocalDate.of(2014, 11, 20));
        assertThat(a.getExpirationDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(a.getReissueDate()).isEqualTo(LocalDate.of(2025, 5, 20));
        assertThat(a.getCurrCycCredit()).isEqualByComparingTo("0");
        assertThat(a.getCurrCycDebit()).isEqualByComparingTo("0");
        // The sample data puts the disclosure-group-like value in ACCT-ADDR-ZIP and leaves ACCT-GROUP-ID blank.
        assertThat(a.getAddrZip()).isEqualTo("A000000000");
        assertThat(a.getGroupId()).isNull();
    }

    @Test
    void customerRecord() {
        Customer c = ascii(SampleDataset.CUSTDATA, CopybookLayouts.CUSTOMER).get(0);
        assertThat(c.getCustId()).isEqualTo(1L);
        assertThat(c.getFirstName()).isEqualTo("Immanuel");
        assertThat(c.getMiddleName()).isEqualTo("Madeline");
        assertThat(c.getLastName()).isEqualTo("Kessler");
        assertThat(c.getAddrLine1()).isEqualTo("618 Deshaun Route");
        assertThat(c.getAddrLine2()).isEqualTo("Apt. 802");
        assertThat(c.getAddrLine3()).isEqualTo("Altenwerthshire");
        assertThat(c.getAddrStateCd()).isEqualTo("NC");
        assertThat(c.getAddrCountryCd()).isEqualTo("USA");
        assertThat(c.getAddrZip()).isEqualTo("12546");
        assertThat(c.getPhoneNum1()).isEqualTo("(908)119-8310");
        assertThat(c.getPhoneNum2()).isEqualTo("(373)693-8684");
        assertThat(c.getSsn()).isEqualTo(20973888L);
        assertThat(c.getGovtIssuedId()).isEqualTo("00000000000049368437");
        assertThat(c.getDob()).isEqualTo(LocalDate.of(1961, 6, 8));
        assertThat(c.getEftAccountId()).isEqualTo("0053581756");
        assertThat(c.getPriCardHolderInd()).isEqualTo('Y');
        assertThat(c.getFicoScore()).isEqualTo(274);
    }

    @Test
    void cardAndXrefRecords() {
        Card card = ascii(SampleDataset.CARDDATA, CopybookLayouts.CARD).get(0);
        assertThat(card.getCardNum()).isEqualTo("0500024453765740");
        assertThat(card.getAcctId()).isEqualTo(50L);
        assertThat(card.getCvvCd()).isEqualTo(747);
        assertThat(card.getEmbossedName()).isEqualTo("Aniya Von");
        assertThat(card.getExpirationDate()).isEqualTo(LocalDate.of(2023, 3, 9));
        assertThat(card.getActiveStatus()).isEqualTo('Y');

        CardXref x = ascii(SampleDataset.CARDXREF, CopybookLayouts.CARD_XREF).get(0);
        assertThat(x.getXrefCardNum()).isEqualTo("0500024453765740");
        assertThat(x.getXrefCustId()).isEqualTo(50L);
        assertThat(x.getXrefAcctId()).isEqualTo(50L);
    }

    @Test
    void referenceAndBalanceRecords() {
        TransactionCategoryBalance b = ascii(SampleDataset.TCATBAL, CopybookLayouts.TRAN_CAT_BALANCE).get(0);
        assertThat(b.getAcctId()).isEqualTo(1L);
        assertThat(b.getTypeCd()).isEqualTo("01");
        assertThat(b.getCatCd()).isEqualTo(1);
        assertThat(b.getBalance()).isEqualByComparingTo("0");

        DisclosureGroup g = ascii(SampleDataset.DISCGRP, CopybookLayouts.DISCLOSURE_GROUP).get(0);
        assertThat(g.getAcctGroupId()).isEqualTo("A000000000");
        assertThat(g.getTranTypeCd()).isEqualTo("01");
        assertThat(g.getTranCatCd()).isEqualTo(1);
        assertThat(g.getIntRate()).isEqualByComparingTo("15.00");

        TransactionType t = ascii(SampleDataset.TRANTYPE, CopybookLayouts.TRANSACTION_TYPE).get(0);
        assertThat(t.getTranType()).isEqualTo("01");
        assertThat(t.getTranTypeDesc()).isEqualTo("Purchase");

        TransactionCategory c = ascii(SampleDataset.TRANCATG, CopybookLayouts.TRANSACTION_CATEGORY).get(0);
        assertThat(c.getTranTypeCd()).isEqualTo("01");
        assertThat(c.getTranCatCd()).isEqualTo(1);
        assertThat(c.getTranCatTypeDesc()).isEqualTo("Regular Sales Draft");
    }

    @Test
    void dailyTransactionRecord() {
        DailyTransaction t = ascii(SampleDataset.DALYTRAN, CopybookLayouts.DAILY_TRANSACTION).get(0);
        assertThat(t.getTranId()).isEqualTo("0000000000683580");
        assertThat(t.getTypeCd()).isEqualTo("01");
        assertThat(t.getCatCd()).isEqualTo(1);
        assertThat(t.getSource()).isEqualTo("POS TERM");
        assertThat(t.getDescription()).isEqualTo("Purchase at Abshire-Lowe");
        assertThat(t.getAmount()).isEqualByComparingTo("504.77");
        assertThat(t.getMerchantId()).isEqualTo(800000000L);
        assertThat(t.getMerchantName()).isEqualTo("Abshire-Lowe");
        assertThat(t.getMerchantCity()).isEqualTo("North Enoshaven");
        assertThat(t.getMerchantZip()).isEqualTo("72112");
        assertThat(t.getCardNum()).isEqualTo("4859452612877065");
        assertThat(t.getOrigTs()).isEqualTo(LocalDateTime.of(2022, 6, 10, 19, 27, 53));
        assertThat(t.getProcTs()).isNull();
    }

    @Test
    void usersOnlyShipAsEbcdic() {
        List<User> users = ebcdic(SampleDataset.USRSEC, CopybookLayouts.USER);
        assertThat(users).hasSize(10);
        User admin = users.get(0);
        assertThat(admin.getUsrId()).isEqualTo("ADMIN001");
        assertThat(admin.getFirstName()).isEqualTo("MARGARET");
        assertThat(admin.getLastName()).isEqualTo("GOLD");
        assertThat(admin.getPwd()).isEqualTo("PASSWORD");
        assertThat(admin.getUsrType()).isEqualTo('A');
        assertThat(users).extracting(User::getUsrType).containsOnly('A', 'U');
    }

    @Test
    void recordCounts() {
        assertThat(ascii(SampleDataset.ACCTDATA, CopybookLayouts.ACCOUNT)).hasSize(50);
        assertThat(ascii(SampleDataset.CUSTDATA, CopybookLayouts.CUSTOMER)).hasSize(50);
        assertThat(ascii(SampleDataset.CARDDATA, CopybookLayouts.CARD)).hasSize(50);
        assertThat(ascii(SampleDataset.CARDXREF, CopybookLayouts.CARD_XREF)).hasSize(50);
        assertThat(ascii(SampleDataset.TCATBAL, CopybookLayouts.TRAN_CAT_BALANCE)).hasSize(50);
        assertThat(ascii(SampleDataset.DISCGRP, CopybookLayouts.DISCLOSURE_GROUP)).hasSize(51);
        assertThat(ascii(SampleDataset.TRANTYPE, CopybookLayouts.TRANSACTION_TYPE)).hasSize(7);
        assertThat(ascii(SampleDataset.TRANCATG, CopybookLayouts.TRANSACTION_CATEGORY)).hasSize(18);
        assertThat(ascii(SampleDataset.DALYTRAN, CopybookLayouts.DAILY_TRANSACTION)).hasSize(300);
    }

    /**
     * The ASCII and EBCDIC copies are the same data except for two records, which this test pins
     * so any further drift is caught.
     */
    @Test
    void asciiAndEbcdicCopiesMatchExceptTwoKnownRecords() {
        assertSame(SampleDataset.CUSTDATA, CopybookLayouts.CUSTOMER);
        assertSame(SampleDataset.CARDDATA, CopybookLayouts.CARD);
        assertSame(SampleDataset.CARDXREF, CopybookLayouts.CARD_XREF);
        assertSame(SampleDataset.TCATBAL, CopybookLayouts.TRAN_CAT_BALANCE);
        assertSame(SampleDataset.TRANTYPE, CopybookLayouts.TRANSACTION_TYPE);
        assertSame(SampleDataset.TRANCATG, CopybookLayouts.TRANSACTION_CATEGORY);
        assertSame(SampleDataset.DALYTRAN, CopybookLayouts.DAILY_TRANSACTION);

        List<Account> aa = ascii(SampleDataset.ACCTDATA, CopybookLayouts.ACCOUNT);
        List<Account> ea = ebcdic(SampleDataset.ACCTDATA, CopybookLayouts.ACCOUNT);
        assertThat(ea).usingRecursiveFieldByFieldElementComparatorIgnoringFields("addrZip")
                .containsExactlyElementsOf(aa);
        assertThat(ea.get(48).getAcctId()).isEqualTo(49L);
        assertThat(aa.get(48).getAddrZip()).isEqualTo("A000000000");
        assertThat(ea.get(48).getAddrZip()).isEqualTo("ZEROAPR");

        List<DisclosureGroup> ad = ascii(SampleDataset.DISCGRP, CopybookLayouts.DISCLOSURE_GROUP);
        List<DisclosureGroup> ed = ebcdic(SampleDataset.DISCGRP, CopybookLayouts.DISCLOSURE_GROUP);
        assertThat(ed).usingRecursiveFieldByFieldElementComparatorIgnoringFields("intRate")
                .containsExactlyElementsOf(ad);
        List<String> rateDiffs = new ArrayList<>();
        for (int i = 0; i < ad.size(); i++) {
            if (ad.get(i).getIntRate().compareTo(ed.get(i).getIntRate()) != 0) {
                DisclosureGroup g = ad.get(i);
                rateDiffs.add(g.getAcctGroupId() + "/" + g.getTranTypeCd() + "/" + g.getTranCatCd()
                        + ": " + g.getIntRate() + " vs " + ed.get(i).getIntRate());
            }
        }
        assertThat(rateDiffs).containsExactly("DEFAULT/07/1: 0.00 vs 15.00");
    }

    private <T> void assertSame(SampleDataset d, Layout<T> l) {
        assertThat(ebcdic(d, l)).usingRecursiveFieldByFieldElementComparator().containsExactlyElementsOf(ascii(d, l));
    }

    /**
     * CBEXPORT output: 500 records mixing zoned, COMP and COMP-3 fields. Transactions match
     * DALYTRAN exactly; masters match the ASCII copies except a handful of records the export
     * captured in a different state.
     */
    @Test
    void exportFileReconcilesWithMasterFiles() {
        List<ExportLayout.Decoded> exp = SampleDataFiles.readAll(
                files.reader(SampleDataset.EXPORT, ExportLayout.RECORD_LENGTH, Encoding.EBCDIC), ExportLayout::decode);
        assertThat(exp).hasSize(500);
        Map<Character, Long> counts = exp.stream()
                .collect(Collectors.groupingBy(d -> d.header().getRecType(), Collectors.counting()));
        assertThat(counts).containsExactlyInAnyOrderEntriesOf(Map.of('C', 50L, 'A', 50L, 'X', 50L, 'T', 300L, 'D', 50L));
        assertThat(exp.get(0).header().getBranchId()).isEqualTo("0001");
        assertThat(exp.get(0).header().getRegionCode()).isEqualTo("NORTH");
        assertThat(exp.get(0).header().getExportTimestamp()).isEqualTo(LocalDateTime.of(2025, 9, 28, 22, 53, 40));

        List<Transaction> tx = payloads(exp, Transaction.class);
        assertThat(tx).usingRecursiveFieldByFieldElementComparator()
                .containsExactlyElementsOf(ascii(SampleDataset.DALYTRAN, CopybookLayouts.TRANSACTION));

        TreeSet<String> mismatches = new TreeSet<>();
        mismatches.addAll(diff("C", payloads(exp, Customer.class),
                ascii(SampleDataset.CUSTDATA, CopybookLayouts.CUSTOMER), c -> c.getCustId().toString()));
        mismatches.addAll(diff("A", payloads(exp, Account.class),
                ascii(SampleDataset.ACCTDATA, CopybookLayouts.ACCOUNT), a -> a.getAcctId().toString()));
        mismatches.addAll(diff("X", payloads(exp, CardXref.class),
                ascii(SampleDataset.CARDXREF, CopybookLayouts.CARD_XREF), CardXref::getXrefCardNum));
        mismatches.addAll(diff("D", payloads(exp, Card.class),
                ascii(SampleDataset.CARDDATA, CopybookLayouts.CARD), Card::getCardNum));
        assertThat(mismatches).containsExactly("A1", "A49", "A50", "C1", "C50");
    }

    private static <T> List<T> payloads(List<ExportLayout.Decoded> exp, Class<T> type) {
        return exp.stream().map(ExportLayout.Decoded::payload).filter(type::isInstance).map(type::cast).toList();
    }

    private static <T> List<String> diff(String tag, List<T> exported, List<T> master, Function<T, String> key) {
        Map<String, T> byKey = master.stream().collect(Collectors.toMap(key, Function.identity()));
        assertThat(exported).hasSameSizeAs(master);
        List<String> out = new ArrayList<>();
        for (T e : exported) {
            T m = byKey.get(key.apply(e));
            if (m == null || !recursiveEquals(e, m)) {
                out.add(tag + key.apply(e));
            }
        }
        return out;
    }

    private static boolean recursiveEquals(Object a, Object b) {
        try {
            assertThat(a).usingRecursiveComparison().withComparatorForType(
                    (x, y) -> x.compareTo(y), java.math.BigDecimal.class).isEqualTo(b);
            return true;
        } catch (AssertionError e) {
            return Objects.equals(a, b);
        }
    }
}
