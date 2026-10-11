# Modernization: INTCALC / CBACT04C → Java

This shows test-first migration of one legacy batch program, monthly interest calculation, out of the CardDemo mainframe app.

```
modern/
  golden-master/   runs the UNMODIFIED legacy COBOL (GnuCOBOL) and records its outputs
    cobol/         harness only: data loader, JCL-step driver, CEE3ABD stub, output dumper
    fixtures/      input scenarios (seed = repo data, month-end = rule-exercising balances)
    expected/      golden outputs produced by the legacy program
  interest-service/  Java 17 migration with characterization and rule tests
```

## Run

```bash
sudo apt-get install -y gnucobol maven       # GnuCOBOL 3.x, Java 17+
modern/golden-master/run-cobol.sh month-end  # regenerate golden master from legacy COBOL
modern/golden-master/run-cobol.sh seed
cd modern/interest-service && mvn test       # Java must match the golden master
```

### Live demo helper

Open this branch in GitHub Codespaces (`.devcontainer/` installs GnuCOBOL, Java 17 and Maven), then:

```bash
modern/demo.sh check    # tool versions
modern/demo.sh golden   # run the unmodified COBOL, regenerate the golden master
modern/demo.sh test     # 14 parity + business-rule tests
modern/demo.sh bug      # legacy defect LD-1: last account never posted
modern/demo.sh break    # truncation -> rounding: tests fail (-10.41 vs -10.42)
modern/demo.sh fix      # revert: tests pass
```

## Business rules (extracted from `app/cbl/CBACT04C.cbl`)

| ID | Rule | COBOL source | Test |
|---|---|---|---|
| BR-1 | Balances are processed in key order (account, type, category). Interest is accumulated per account and posted when the account changes. | main loop, `1050-UPDATE-ACCOUNT` | GoldenMasterTest |
| BR-2 | Monthly interest = balance × annual rate / 1200, **truncated** to 2 decimals (no `ROUNDED`). Negative balances truncate toward zero. | `1300-COMPUTE-INTEREST` | InterestRulesTest |
| BR-3 | The rate comes from the disclosure group keyed by account group + type + category. | `1200-GET-INTEREST-RATE` | InterestRulesTest |
| BR-4 | If the group record is missing (status 23), the `DEFAULT` group is used. If that is also missing, the job abends. | `1200-A-GET-DEFAULT-INT-RATE` | InterestRulesTest |
| BR-5 | A zero rate produces no transaction. The account is still rewritten. | main loop | InterestRulesTest |
| BR-6 | Transaction id = JCL PARM date (10) + run-wide 6-digit sequence. | `1300-B-WRITE-TX` | InterestRulesTest |
| BR-7 | Interest transaction: type `01`, category `0005`, source `System`, description `Int. for a/c <acct>`, card number from the xref file, DB2-format timestamp. | `1300-B-WRITE-TX` | InterestRulesTest |
| BR-8 | Posting adds total interest to `ACCT-CURR-BAL` and resets cycle credit and debit to 0. | `1050-UPDATE-ACCOUNT` | GoldenMasterTest |
| — | Fees are not implemented (`1400-COMPUTE-FEES` is a stub). | `1400-COMPUTE-FEES` | — |

## Legacy defect found by the golden master

**LD-1: the last account's interest is never posted to its balance.** The `PERFORM UNTIL END-OF-FILE = 'Y'` loop exits as soon as EOF is reached, so the `ELSE PERFORM 1050-UPDATE-ACCOUNT` branch never runs. The last account gets its interest *transaction* written, but its balance and cycle totals are not updated. You can see this in `golden-master/expected/month-end/acctdata.txt`: account 5 is unchanged.

The Java service keeps this behaviour in `Mode.LEGACY_PARITY`, so parity can be proven. It also offers `Mode.CORRECTED`, which fixes it. Whether to fix it is a business decision.

## Copybook → Java mapping

| Copybook | Record | Java |
|---|---|---|
| CVTRA01Y | TRAN-CAT-BAL-RECORD (50) | `TransactionCategoryBalance` |
| CVACT03Y | CARD-XREF-RECORD (50) | `CardXref` |
| CVTRA02Y | DIS-GROUP-RECORD (50) | `DisclosureGroup` |
| CVACT01Y | ACCOUNT-RECORD (300) | `Account` (raw record kept, so REWRITE is byte-exact) |
| CVTRA05Y | TRAN-RECORD (350) | `InterestTransaction` |
| `PIC S9(n)V99` (zoned, overpunch sign) | | `ZonedDecimal` ↔ `BigDecimal` |
