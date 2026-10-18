package com.carddemo.service;

/** {@code WS-RETURN-MSG} / {@code WS-INFO-MSG} literals, copied from the COBOL source. */
public final class CardLookupMessages {

    /** COCRDSLC {@code NO-SEARCH-CRITERIA-RECEIVED}. */
    public static final String NO_INPUT = "No input received";
    /** COCRDSLC {@code WS-PROMPT-FOR-ACCT}. */
    public static final String ACCT_NOT_PROVIDED = "Account number not provided";
    /** COCRDSLC {@code WS-PROMPT-FOR-CARD}. */
    public static final String CARD_NOT_PROVIDED = "Card number not provided";
    /** COCRDSLC 2210-EDIT-ACCOUNT, literal moved to WS-RETURN-MSG. */
    public static final String ACCT_NOT_NUMERIC = "ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER";
    /** COCRDSLC 2220-EDIT-CARD, literal moved to WS-RETURN-MSG. */
    public static final String CARD_NOT_NUMERIC = "CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER";
    /** COCRDSLC {@code DID-NOT-FIND-ACCTCARD-COMBO} (9100-GETCARD-BYACCTCARD, NOTFND). */
    public static final String CARD_NOT_FOUND = "Did not find cards for this search condition";
    /** COCRDSLC {@code DID-NOT-FIND-ACCT-IN-CARDXREF} (9150-GETCARD-BYACCT, NOTFND on CARDAIX). */
    public static final String ACCT_HAS_NO_CARDS = "Did not find this account in cards database";
    /** COCRDSLC {@code XREF-READ-ERROR}. */
    public static final String READ_ERROR = "Error reading Card Data File";
    /** COACTVWC {@code DID-NOT-FIND-ACCT-IN-CARDXREF}. */
    public static final String CARD_NOT_IN_XREF = "Did not find this account in account card xref file";
    /** COACTVWC {@code DID-NOT-FIND-ACCT-IN-ACCTDAT}. */
    public static final String ACCT_NOT_IN_MASTER = "Did not find this account in account master file";
    /** COACTVWC {@code DID-NOT-FIND-CUST-IN-CUSTDAT}. */
    public static final String CUST_NOT_IN_MASTER = "Did not find associated customer in master file";
    /** COCRDSLC {@code FOUND-CARDS-FOR-ACCOUNT}, leading spaces trimmed. */
    public static final String FOUND = "Displaying requested details";

    private CardLookupMessages() {
    }
}
