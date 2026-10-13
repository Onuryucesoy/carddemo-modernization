package com.carddemo.interest;

import java.util.List;

/** Everything the run produces: SYSOUT, TRANSACT, the account master afterwards, and RC/abend. */
public final class BatchResult {
    private final List<String> sysout;
    private final List<InterestTransaction> transactions;
    private final List<Account> accounts;
    private final String result;

    BatchResult(List<String> sysout, List<InterestTransaction> transactions, List<Account> accounts, String result) {
        this.sysout = List.copyOf(sysout);
        this.transactions = List.copyOf(transactions);
        this.accounts = List.copyOf(accounts);
        this.result = result;
    }

    public List<String> sysout() { return sysout; }
    public List<InterestTransaction> transactions() { return transactions; }
    /** Account master after the run, in key order. */
    public List<Account> accounts() { return accounts; }
    /** {@code RC=0000} or {@code ABEND U0999}. */
    public String result() { return result; }
    public boolean abended() { return result.startsWith("ABEND"); }

    public Account account(long accountId) {
        String id = String.format("%011d", accountId);
        return accounts.stream().filter(a -> a.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no account " + id));
    }
}
