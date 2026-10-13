package com.carddemo.interest;

import java.math.BigDecimal;

/** Interest transaction written to TRANSACT, copybook CVTRA05Y (350 bytes). Paragraph 1300-B-WRITE-TX. */
public final class InterestTransaction {
    public static final int LENGTH = 350;
    static final String TYPE_CODE = "01";
    static final String CATEGORY_CODE = "0005";
    static final String SOURCE = "System";

    private final String id;
    private final String accountId;
    private final String cardNumber;
    private final BigDecimal amount;
    private final String timestamp;

    InterestTransaction(String id, String accountId, String cardNumber, BigDecimal amount, String timestamp) {
        this.id = id;
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String id() { return id; }
    public String accountId() { return accountId; }
    public String cardNumber() { return cardNumber; }
    public BigDecimal amount() { return amount; }
    public String timestamp() { return timestamp; }

    public String record() {
        StringBuilder r = new StringBuilder(LENGTH);
        r.append(pad(id, 16))
                .append(TYPE_CODE)
                .append(CATEGORY_CODE)
                .append(pad(SOURCE, 10))
                .append(pad("Int. for a/c " + accountId, 100))
                .append(ZonedDecimal.format(amount, 9, 2))
                .append("000000000")
                .append(pad("", 50))
                .append(pad("", 50))
                .append(pad("", 10))
                .append(pad(cardNumber, 16))
                .append(pad(timestamp, 26))
                .append(pad(timestamp, 26))
                .append(pad("", 20));
        return r.toString();
    }

    private static String pad(String s, int len) {
        return String.format("%-" + len + "." + len + "s", s);
    }
}
