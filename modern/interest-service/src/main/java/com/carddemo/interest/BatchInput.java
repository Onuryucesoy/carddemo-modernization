package com.carddemo.interest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** The four input datasets of the INTCALC step (TCATBALF, XREFFILE, ACCTFILE, DISCGRP). */
public final class BatchInput {
    final List<TranCatBalance> balances;
    final Map<String, CardXref> xrefByAccount;
    final TreeMap<String, Account> accounts;
    final Map<String, DisclosureGroup> disclosureGroups;

    private BatchInput(List<TranCatBalance> balances, Map<String, CardXref> xrefByAccount,
                       TreeMap<String, Account> accounts, Map<String, DisclosureGroup> disclosureGroups) {
        this.balances = balances;
        this.xrefByAccount = xrefByAccount;
        this.accounts = accounts;
        this.disclosureGroups = disclosureGroups;
    }

    public static BatchInput fromRecords(List<String> tcatbal, List<String> xref, List<String> accounts, List<String> discgrp) {
        // TCATBALF is a KSDS read sequentially, i.e. in key order.
        List<TranCatBalance> balances = tcatbal.stream().filter(BatchInput::present).map(TranCatBalance::new)
                .sorted(Comparator.comparing(TranCatBalance::key)).toList();
        Map<String, CardXref> xrefs = new HashMap<>();
        xref.stream().filter(BatchInput::present).map(CardXref::new).forEach(x -> xrefs.putIfAbsent(x.accountId(), x));
        TreeMap<String, Account> accts = new TreeMap<>();
        accounts.stream().filter(BatchInput::present).map(Account::new).forEach(a -> accts.put(a.id(), a));
        Map<String, DisclosureGroup> groups = new HashMap<>();
        discgrp.stream().filter(BatchInput::present).map(DisclosureGroup::new).forEach(g -> groups.put(g.key(), g));
        return new BatchInput(balances, xrefs, accts, groups);
    }

    public static BatchInput read(Path dir) throws IOException {
        return fromRecords(lines(dir, "tcatbal.txt"), lines(dir, "cardxref.txt"),
                lines(dir, "acctdata.txt"), lines(dir, "discgrp.txt"));
    }

    private static List<String> lines(Path dir, String name) throws IOException {
        return Files.readAllLines(dir.resolve(name), StandardCharsets.US_ASCII);
    }

    private static boolean present(String line) {
        return !line.replace("\r", "").isEmpty();
    }
}
