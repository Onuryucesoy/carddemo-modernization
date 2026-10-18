package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.entity.ExportRecord;
import com.carddemo.entity.Transaction;
import java.math.BigDecimal;

/**
 * {@code CVEXPORT} (CBEXPORT output, 500-byte records). The header is fixed; the 460-byte
 * {@code EXPORT-RECORD-DATA} area is REDEFINED per {@code EXPORT-REC-TYPE} and mixes zoned,
 * {@code COMP} (binary) and {@code COMP-3} (packed) fields, so it is decoded from the raw bytes.
 */
public final class ExportLayout {

    public static final int RECORD_LENGTH = 500;
    public static final int HEADER_LENGTH = 40;
    public static final int DATA_LENGTH = 460;

    public static final char CUSTOMER = 'C';
    public static final char ACCOUNT = 'A';
    public static final char XREF = 'X';
    public static final char TRANSACTION = 'T';
    public static final char CARD = 'D';

    /** One decoded export record: header plus the payload entity for its record type. */
    public record Decoded(ExportRecord header, Object payload) {
    }

    private ExportLayout() {
    }

    public static Decoded decode(FixedRecord r) {
        ExportRecord h = new ExportRecord();
        h.setRecType(r.flag());
        h.setExportTimestamp(r.timestamp());
        h.setSequenceNum(r.binary(9));
        h.setBranchId(r.text(4));
        h.setRegionCode(r.text(5));
        Object payload = switch (h.getRecType()) {
            case CUSTOMER -> customer(r);
            case ACCOUNT -> account(r);
            case XREF -> xref(r);
            case TRANSACTION -> transaction(r);
            case CARD -> card(r);
            default -> throw new IllegalArgumentException(
                    "Unknown EXPORT-REC-TYPE at sequence " + h.getSequenceNum());
        };
        r.assertFullyConsumed("CVEXPORT");
        h.setRecordKey(keyOf(payload));
        return new Decoded(h, payload);
    }

    private static String keyOf(Object payload) {
        if (payload instanceof Customer c) {
            return String.valueOf(c.getCustId());
        } else if (payload instanceof Account a) {
            return String.valueOf(a.getAcctId());
        } else if (payload instanceof CardXref x) {
            return x.getXrefCardNum();
        } else if (payload instanceof Transaction t) {
            return t.getTranId();
        }
        return ((Card) payload).getCardNum();
    }

    private static Customer customer(FixedRecord r) {
        Customer c = new Customer();
        c.setCustId(r.binary(9));
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
        c.setFicoScore(r.packed(3, 0).intValueExact());
        r.skip(134);
        return c;
    }

    private static Account account(FixedRecord r) {
        Account a = new Account();
        a.setAcctId(r.unsigned(11));
        a.setActiveStatus(r.flag());
        a.setCurrBal(r.packed(12, 2));
        a.setCreditLimit(r.zoned(12, 2));
        a.setCashCreditLimit(r.packed(12, 2));
        a.setOpenDate(r.date());
        a.setExpirationDate(r.date());
        a.setReissueDate(r.date());
        a.setCurrCycCredit(r.zoned(12, 2));
        a.setCurrCycDebit(BigDecimal.valueOf(r.binary(12), 2));
        a.setAddrZip(r.text(10));
        a.setGroupId(r.text(10));
        r.skip(352);
        return a;
    }

    private static Transaction transaction(FixedRecord r) {
        Transaction t = new Transaction();
        t.setTranId(r.text(16));
        t.setTypeCd(r.text(2));
        t.setCatCd(r.unsignedInt(4));
        t.setSource(r.text(10));
        t.setDescription(r.text(100));
        t.setAmount(r.packed(11, 2));
        t.setMerchantId(r.binary(9));
        t.setMerchantName(r.text(50));
        t.setMerchantCity(r.text(50));
        t.setMerchantZip(r.text(10));
        t.setCardNum(r.text(16));
        t.setOrigTs(r.timestamp());
        t.setProcTs(r.timestamp());
        r.skip(140);
        return t;
    }

    private static CardXref xref(FixedRecord r) {
        CardXref x = new CardXref();
        x.setXrefCardNum(r.text(16));
        x.setXrefCustId(r.unsigned(9));
        x.setXrefAcctId(r.binary(11));
        r.skip(427);
        return x;
    }

    private static Card card(FixedRecord r) {
        Card c = new Card();
        c.setCardNum(r.text(16));
        c.setAcctId(r.binary(11));
        c.setCvvCd(Math.toIntExact(r.binary(3)));
        c.setEmbossedName(r.text(50));
        c.setExpirationDate(r.date());
        c.setActiveStatus(r.flag());
        r.skip(373);
        return c;
    }
}
