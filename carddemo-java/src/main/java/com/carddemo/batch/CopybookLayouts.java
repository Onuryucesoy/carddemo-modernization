package com.carddemo.batch;

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
import java.util.function.Function;

/**
 * Record mappers, one per copybook. Fields are read in copybook order; FILLER is skipped.
 */
public final class CopybookLayouts {

    /** A copybook record layout: its length in bytes and how to map it to an entity. */
    public record Layout<T>(String copybook, int recordLength, Function<FixedRecord, T> mapper) {

        public T map(FixedRecord r) {
            T entity = mapper.apply(r);
            r.assertFullyConsumed(copybook);
            return entity;
        }
    }

    public static final Layout<Account> ACCOUNT = new Layout<>("CVACT01Y", 300, CopybookLayouts::account);
    public static final Layout<Customer> CUSTOMER = new Layout<>("CVCUS01Y", 500, CopybookLayouts::customer);
    public static final Layout<Card> CARD = new Layout<>("CVACT02Y", 150, CopybookLayouts::card);
    public static final Layout<CardXref> CARD_XREF = new Layout<>("CVACT03Y", 50, CopybookLayouts::cardXref);
    public static final Layout<TransactionCategoryBalance> TRAN_CAT_BALANCE =
            new Layout<>("CVTRA01Y", 50, CopybookLayouts::tranCatBalance);
    public static final Layout<DisclosureGroup> DISCLOSURE_GROUP =
            new Layout<>("CVTRA02Y", 50, CopybookLayouts::disclosureGroup);
    public static final Layout<TransactionType> TRANSACTION_TYPE =
            new Layout<>("CVTRA03Y", 60, CopybookLayouts::transactionType);
    public static final Layout<TransactionCategory> TRANSACTION_CATEGORY =
            new Layout<>("CVTRA04Y", 60, CopybookLayouts::transactionCategory);
    public static final Layout<Transaction> TRANSACTION = new Layout<>("CVTRA05Y", 350, CopybookLayouts::transaction);
    public static final Layout<DailyTransaction> DAILY_TRANSACTION =
            new Layout<>("CVTRA06Y", 350, CopybookLayouts::dailyTransaction);
    public static final Layout<User> USER = new Layout<>("CSUSR01Y", 80, CopybookLayouts::user);

    private CopybookLayouts() {
    }

    static Account account(FixedRecord r) {
        Account a = new Account();
        a.setAcctId(r.unsigned(11));
        a.setActiveStatus(r.flag());
        a.setCurrBal(r.zoned(12, 2));
        a.setCreditLimit(r.zoned(12, 2));
        a.setCashCreditLimit(r.zoned(12, 2));
        a.setOpenDate(r.date());
        a.setExpirationDate(r.date());
        a.setReissueDate(r.date());
        a.setCurrCycCredit(r.zoned(12, 2));
        a.setCurrCycDebit(r.zoned(12, 2));
        a.setAddrZip(r.text(10));
        a.setGroupId(r.text(10));
        r.skip(178);
        return a;
    }

    /** Also used for {@code CUSTREC}, which has the same layout as {@code CVCUS01Y}. */
    static Customer customer(FixedRecord r) {
        Customer c = new Customer();
        c.setCustId(r.unsigned(9));
        c.setFirstName(r.text(25));
        c.setMiddleName(r.text(25));
        c.setLastName(r.text(25));
        c.setAddrLine1(r.text(50));
        c.setAddrLine2(r.text(50));
        c.setAddrLine3(r.text(50));
        c.setAddrStateCd(r.text(2));
        c.setAddrCountryCd(r.text(3));
        c.setAddrZip(r.text(10));
        c.setPhoneNum1(r.text(15));
        c.setPhoneNum2(r.text(15));
        c.setSsn(r.unsigned(9));
        c.setGovtIssuedId(r.text(20));
        c.setDob(r.date());
        c.setEftAccountId(r.text(10));
        c.setPriCardHolderInd(r.flag());
        c.setFicoScore(r.unsignedInt(3));
        r.skip(168);
        return c;
    }

    static Card card(FixedRecord r) {
        Card c = new Card();
        c.setCardNum(r.text(16));
        c.setAcctId(r.unsigned(11));
        c.setCvvCd(r.unsignedInt(3));
        c.setEmbossedName(r.text(50));
        c.setExpirationDate(r.date());
        c.setActiveStatus(r.flag());
        r.skip(59);
        return c;
    }

    static CardXref cardXref(FixedRecord r) {
        CardXref x = new CardXref();
        x.setXrefCardNum(r.text(16));
        x.setXrefCustId(r.unsigned(9));
        x.setXrefAcctId(r.unsigned(11));
        r.skip(14);
        return x;
    }

    static TransactionCategoryBalance tranCatBalance(FixedRecord r) {
        TransactionCategoryBalance b = new TransactionCategoryBalance();
        b.setAcctId(r.unsigned(11));
        b.setTypeCd(r.text(2));
        b.setCatCd(r.unsignedInt(4));
        b.setBalance(r.zoned(11, 2));
        r.skip(22);
        return b;
    }

    static DisclosureGroup disclosureGroup(FixedRecord r) {
        DisclosureGroup g = new DisclosureGroup();
        g.setAcctGroupId(r.text(10));
        g.setTranTypeCd(r.text(2));
        g.setTranCatCd(r.unsignedInt(4));
        g.setIntRate(r.zoned(6, 2));
        r.skip(28);
        return g;
    }

    static TransactionType transactionType(FixedRecord r) {
        TransactionType t = new TransactionType();
        t.setTranType(r.text(2));
        t.setTranTypeDesc(r.text(50));
        r.skip(8);
        return t;
    }

    static TransactionCategory transactionCategory(FixedRecord r) {
        TransactionCategory c = new TransactionCategory();
        c.setTranTypeCd(r.text(2));
        c.setTranCatCd(r.unsignedInt(4));
        c.setTranCatTypeDesc(r.text(50));
        r.skip(4);
        return c;
    }

    static Transaction transaction(FixedRecord r) {
        Transaction t = new Transaction();
        t.setTranId(r.text(16));
        t.setTypeCd(r.text(2));
        t.setCatCd(r.unsignedInt(4));
        t.setSource(r.text(10));
        t.setDescription(r.text(100));
        t.setAmount(r.zoned(11, 2));
        t.setMerchantId(r.unsigned(9));
        t.setMerchantName(r.text(50));
        t.setMerchantCity(r.text(50));
        t.setMerchantZip(r.text(10));
        t.setCardNum(r.text(16));
        t.setOrigTs(r.timestamp());
        t.setProcTs(r.timestamp());
        r.skip(20);
        return t;
    }

    static DailyTransaction dailyTransaction(FixedRecord r) {
        DailyTransaction t = new DailyTransaction();
        t.setTranId(r.text(16));
        t.setTypeCd(r.text(2));
        t.setCatCd(r.unsignedInt(4));
        t.setSource(r.text(10));
        t.setDescription(r.text(100));
        t.setAmount(r.zoned(11, 2));
        t.setMerchantId(r.unsigned(9));
        t.setMerchantName(r.text(50));
        t.setMerchantCity(r.text(50));
        t.setMerchantZip(r.text(10));
        t.setCardNum(r.text(16));
        t.setOrigTs(r.timestamp());
        t.setProcTs(r.timestamp());
        r.skip(20);
        return t;
    }

    /** {@code SEC-USR-PWD} is returned in clear text; the import job hashes it before writing. */
    static User user(FixedRecord r) {
        User u = new User();
        u.setUsrId(r.text(8));
        u.setFirstName(r.text(20));
        u.setLastName(r.text(20));
        u.setPwd(r.text(8));
        u.setUsrType(r.flag());
        r.skip(23);
        return u;
    }
}
