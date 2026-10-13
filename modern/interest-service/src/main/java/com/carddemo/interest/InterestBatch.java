package com.carddemo.interest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** File-based entry point replacing JCL job INTCALC. Reads/writes the same record layouts as CBACT04C. */
public final class InterestBatch {

    public record Files4(Path tcatbal, Path xref, Path accounts, Path disclosureGroups) {}

    public static InterestCalculator.Result run(Files4 in, String parmDate, Clock clock, InterestCalculator.Mode mode)
            throws IOException {
        Map<String, Account> accounts = new TreeMap<>();
        for (String line : lines(in.accounts())) {
            Account a = Account.parse(line);
            accounts.put(a.id(), a);
        }
        Map<String, CardXref> xref = new LinkedHashMap<>();
        for (String line : lines(in.xref())) {
            CardXref x = CardXref.parse(line);
            xref.putIfAbsent(x.accountId(), x);
        }
        Map<String, DisclosureGroup> groups = new LinkedHashMap<>();
        for (String line : lines(in.disclosureGroups())) {
            DisclosureGroup g = DisclosureGroup.parse(line);
            groups.put(g.key(), g);
        }
        List<TransactionCategoryBalance> balances = lines(in.tcatbal()).stream()
                .map(TransactionCategoryBalance::parse)
                .sorted((a, b) -> (a.accountId() + a.typeCode() + a.categoryCode())
                        .compareTo(b.accountId() + b.typeCode() + b.categoryCode()))
                .collect(Collectors.toCollection(ArrayList::new));
        return new InterestCalculator(accounts, xref, groups, clock, mode).run(balances, parmDate);
    }

    private static List<String> lines(Path p) throws IOException {
        return Files.readAllLines(p).stream()
                .map(l -> l.replace("\r", ""))
                .filter(l -> !l.isBlank())
                .toList();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 6) {
            System.err.println("usage: InterestBatch <tcatbal> <xref> <accounts> <discgrp> <parm-date> <out-dir> [CORRECTED]");
            System.exit(2);
        }
        InterestCalculator.Mode mode = args.length > 6 ? InterestCalculator.Mode.valueOf(args[6])
                : InterestCalculator.Mode.LEGACY_PARITY;
        InterestCalculator.Result result = run(
                new Files4(Path.of(args[0]), Path.of(args[1]), Path.of(args[2]), Path.of(args[3])),
                args[4], Clock.systemDefaultZone(), mode);
        Path out = Path.of(args[5]);
        Files.createDirectories(out);
        Files.write(out.resolve("transact.txt"),
                result.transactions().stream().map(InterestTransaction::toRecord).toList());
        Files.write(out.resolve("acctdata.txt"), result.accounts().stream().map(Account::toRecord).toList());
    }
}
