# carddemo-java

Phase 0/1 of the CardDemo COBOL → Java migration, following the strangler plan in
`docs/migration/cobol-to-java-plan.md` (PR #3). Spring Boot 3.3, Java 17, Maven.

What's here:

- **Phase 1 schema**: JPA entities and Flyway DDL derived field by field from the copybooks in `app/cpy`.
- **Data import**: a Spring Batch job (`sampleDataImportJob`) that loads the sample files in `app/data` (ASCII or EBCDIC) into H2.
- **First vertical slice**: card lookup, replacing `COCRDSLC` (CICS transaction `CCDL` in `app/csd/CARDDEMO.CSD`). It is exposed as REST with HTTP Basic auth backed by the `users` table, which replaces `COSGN00C` for this slice only.

Nothing under `app/` is modified. This project only reads `app/data` at runtime.

## Build and run

```bash
cd carddemo-java
mvn verify                 # unit tests (surefire) + integration tests (failsafe, *IT)
mvn spring-boot:run        # starts on :8080, imports ../app/data into in-memory H2
```

You need JDK 17+ and Maven 3.6+. Run the commands from `carddemo-java/`, because the default data path is `../app/data`.

```bash
curl -u ADMIN001:PASSWORD localhost:8080/api/cards/0500024453765740
curl -u USER0001:PASSWORD 'localhost:8080/api/cards?acctId=00000000050'
```

The sample users and their `PASSWORD` password come from the upstream `USRSEC` sample file. The import
hashes them with BCrypt, but they are well-known demo credentials, so use them only on a dev machine.

Configuration (`src/main/resources/application.yml`):

| Property | Default | Meaning |
|:---|:---|:---|
| `carddemo.import.on-startup` | `true` | Run `sampleDataImportJob` at startup |
| `carddemo.import.data-dir` | `../app/data` | Folder holding `ASCII/` and `EBCDIC/` |
| `carddemo.import.source` | `ASCII` | Which copy to load: `ASCII` or `EBCDIC` |
| `carddemo.import.ebcdic-charset` | `IBM037` | EBCDIC code page (open decision D1: 037 vs 1047) |

Example: `mvn spring-boot:run -Dspring-boot.run.arguments=--carddemo.import.source=EBCDIC`.

## API (COCRDSLC slice)

| Endpoint | COCRDSLC path |
|:---|:---|
| `GET /api/cards/{cardNum}[?acctId=]` | `9100-GETCARD-BYACCTCARD`: read CARDDAT by card number. If `acctId` is given, it must match. |
| `GET /api/cards?acctId=` | `9150-GETCARD-BYACCT`: browse CARDAIX by account, in card-number order. |

Both endpoints require HTTP Basic. `SEC-USR-TYPE = 'A'` gets role `ADMIN` and `'U'` gets role `USER`. As in
`COSGN00C`, the user ID and password are upper-cased before they are compared. All other requests are denied.

Once the card is found, the account and customer are resolved through `CARDXREF`
(`XREF-CARD-NUM` → `XREF-ACCT-ID`, `XREF-CUST-ID`). The top-level JSON fields mirror the COMMAREA
(`COCOM01Y`): `acctId` (CDEMO-ACCT-ID), `acctStatus` (CDEMO-ACCT-STATUS), `cardNum` (CDEMO-CARD-NUM),
`custId` (CDEMO-CUST-ID), and `custFname`/`custMname`/`custLname` (CDEMO-CUST-*). As on the CCRDSLA map,
the card expiry is also returned as separate `expiryMonth` and `expiryYear` fields. CVV, SSN, government
ID and EFT account are not returned.

Example response (shortened):

```json
{
  "acctId": 50, "acctStatus": "Y", "cardNum": "0500024453765740", "custId": 50,
  "custFname": "Aniya", "custMname": "Alba", "custLname": "Von",
  "card": {"embossedName": "Aniya Von", "expirationDate": "2023-03-09", "expiryMonth": "03", "expiryYear": "2023", "activeStatus": "Y"},
  "account": {"activeStatus": "Y", "currBal": 492.00, "creditLimit": 6169.00, "cashCreditLimit": 4587.00, "openDate": "2011-04-22", ...},
  "customer": {"firstName": "Aniya", "lastName": "Von", "addrStateCd": "OR", "ficoScore": 623, ...},
  "infoMessage": "Displaying requested details"
}
```

Error responses have the form `{"status", "error", "message", "field"}` and use the program's own message literals:

| Condition (COCRDSLC paragraph) | HTTP | `message` |
|:---|:---|:---|
| No account and no card (`2200-EDIT-MAP-INPUTS`; `*`, blanks and all zeros count as absent) | 400 | `No input received` |
| Account given but no card, on the card path | 400 | `Card number not provided` |
| Account is not 11 digits (`2210-EDIT-ACCOUNT`) | 400 | `ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER` |
| Card is not 16 digits (`2220-EDIT-CARD`) | 400 | `CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER` |
| Card not on file, or the account filter doesn't match (`9100`) | 404 | `Did not find cards for this search condition` |
| No cards for the account (`9150`) | 404 | `Did not find this account in cards database` |
| Card has no XREF row (COACTVWC literal) | 404 | `Did not find this account in account card xref file` |
| Account from XREF is missing (COACTVWC literal) | 404 | `Did not find this account in account master file` |
| Customer from XREF is missing (COACTVWC literal) | 404 | `Did not find associated customer in master file` |
| Database error (`WHEN OTHER` on the READ) | 500 | `Error reading Card Data File` |

As in the COBOL program, validation runs before any read. COCRDSLC itself only reads CARDDAT. The
XREF/account/customer joins use COACTVWC's lookup order and messages, because the REST response returns
the joined view in a single call.

## Copybook → entity → table

Fields are mapped in copybook order and `FILLER` is dropped. Column names are the COBOL names in
snake_case (`ACCT-CURR-BAL` → `acct_curr_bal`). Each entity field has a Javadoc comment with its COBOL
name and PIC.

| Copybook | Record (file, RECLN) | Entity | Table | Key |
|:---|:---|:---|:---|:---|
| CVACT01Y | ACCOUNT-RECORD (ACCTDAT, 300) | `Account` | `account` | `acct_id` |
| CVCUS01Y | CUSTOMER-RECORD (CUSTDAT, 500) | `Customer` | `customer` | `cust_id` |
| CUSTREC | CUSTOMER-RECORD (same layout as CVCUS01Y; only `CUST-DOB-YYYYMMDD` is named differently) | `Customer` | `customer` | `cust_id` |
| CVACT02Y | CARD-RECORD (CARDDAT, 150) | `Card` | `card` | `card_num`; FK `card_acct_id` → `account` |
| CVACT03Y | CARD-XREF-RECORD (CARDXREF, 50) | `CardXref` | `card_xref` | `xref_card_num`; FKs to `card`, `account`, `customer` |
| CVTRA01Y | TRAN-CAT-BAL-RECORD (TCATBALF, 50) | `TransactionCategoryBalance` | `tran_cat_balance` | `acct_id + type_cd + cat_cd` |
| CVTRA02Y | DIS-GROUP-RECORD (DISCGRP, 50) | `DisclosureGroup` | `disclosure_group` | `acct_group_id + tran_type_cd + tran_cat_cd` |
| CVTRA03Y | TRAN-TYPE-RECORD (TRANTYPE, 60) | `TransactionType` | `transaction_type` | `tran_type` |
| CVTRA04Y | TRAN-CAT-RECORD (TRANCATG, 60) | `TransactionCategory` | `transaction_category` | `tran_type_cd + tran_cat_cd` |
| CVTRA05Y | TRAN-RECORD (TRANSACT, 350) | `Transaction` | `transactions` | `tran_id` |
| CVTRA06Y | DALYTRAN-RECORD (DALYTRAN, 350) | `DailyTransaction` | `daily_transaction` | `dalytran_id` |
| CVTRA07Y | TRANSACTION-DETAIL-REPORT (CBTRN03C print line) | `TransactionReportDetail` | `transaction_report_detail` | `tran_report_trans_id`. Not seeded; the header/total records are print constants. |
| CSUSR01Y | SEC-USER-DATA (USRSEC, 80) | `User` | `users` | `sec_usr_id`; `sec_usr_type IN ('A','U')` |
| CVEXPORT | EXPORT-RECORD header (EXPFILE, 500) | `ExportRecord` | `export_record` | `export_sequence_num` |

PIC mapping:

- `9(n)` → `Long` (n > 9) or `Integer`
- `S9(n)V99` → `BigDecimal(n+2, 2)`
- `X(n)` → `String` (trailing spaces trimmed, all blanks → `null`)
- `X(1)` flags → `Character`
- `X(10)` dates → `LocalDate`
- `X(26)` timestamps → `LocalDateTime`

Notes:

- `CARD-CVV-CD` is `9(03)` → `Integer`, and `CUST-SSN` is `9(09)` → `Long`, as specified. Leading zeros are not kept.
- In the sample data, the disclosure group ID (`A000000000`, `DEFAULT`, `ZEROAPR`) is in `ACCT-ADDR-ZIP`
  and `ACCT-GROUP-ID` is blank. The loader maps strictly by position and leaves the value where it is.
- Each CVEXPORT record carries one `REDEFINES` payload view (customer, account, xref, card or
  transaction). The header of every record goes to `export_record`, and transaction payloads go to
  `transactions`. The other payloads are decoded into the same entity types and reconciled in tests.

## Data loading

For dev/test, `sampleDataImportJob` (package `com.carddemo.batch`) replaces the IDCAMS REPRO loads
(the ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TCATBALF, DISCGRP, TRANTYPE, TRANCATG and DUSRSECJ jobs)
and CBIMPORT. Its steps run in foreign-key order and can be rerun safely:

| File (ASCII / EBCDIC) | Table |
|:---|:---|
| `trantype.txt` / `AWS.M2.CARDDEMO.TRANTYPE.PS` | `transaction_type` |
| `trancatg.txt` / `...TRANCATG.PS` | `transaction_category` |
| `discgrp.txt` / `...DISCGRP.PS` | `disclosure_group` |
| `custdata.txt` / `...CUSTDATA.PS` | `customer` |
| `acctdata.txt` / `...ACCTDATA.PS` | `account` |
| `carddata.txt` / `...CARDDATA.PS` | `card` |
| `cardxref.txt` / `...CARDXREF.PS` | `card_xref` |
| `tcatbal.txt` / `...TCATBALF.PS` | `tran_cat_balance` |
| `dailytran.txt` / `...DALYTRAN.PS` | `daily_transaction` |
| (none) / `...USRSEC.PS`, EBCDIC only | `users` (passwords hashed with BCrypt) |
| (none) / `...EXPORT.DATA.PS`, EBCDIC only | `export_record`, `transactions` |

Decoding (`FixedRecord`):

- **ASCII**: one record per line, cut at the copybook offsets. Signed zoned fields use overpunch
  characters (`{ABCDEFGHI` = +0..9, `}JKLMNOPQR` = -0..9).
- **EBCDIC**: fixed-length blocks with no line separators. Text is decoded with the configured code
  page, and the zoned sign comes from the zone nibble.
- **COMP-3** (packed decimal) and **COMP** (big-endian binary, 2/4/8 bytes depending on digit count)
  occur only in CVEXPORT and are decoded there.
- Every layout checks that it consumed exactly RECLN bytes. Parse errors report only the offset and
  length, never record contents such as card numbers.

Reconciliation results, pinned by `CopybookLayoutsTest`:

- The ASCII and EBCDIC copies are identical except for two records: account 49 (`ACCT-ADDR-ZIP` is
  `A000000000` vs `ZEROAPR`) and disclosure group `DEFAULT/07/0001` (rate 0.00 vs 15.00).
- `EXPORT.DATA.PS` has 500 records (50 C, 50 A, 50 X, 50 D, 300 T). Its transactions match `DALYTRAN`
  exactly. Accounts 1, 49 and 50 and customers 1 and 50 differ from the master files, because the
  export was taken from a different state of the data.

## Tests

| Test | What it covers |
|:---|:---|
| `FixedRecordTest` | Overpunch, EBCDIC zoned, COMP-3, COMP, dates, length checks |
| `CopybookLayoutsTest` | Parses every sample file; checks first-record fields, record counts, and ASCII/EBCDIC/export reconciliation |
| `SchemaMappingTest` | Flyway DDL plus `ddl-auto=validate` for every entity; decimal scale, composite keys, FK and check constraints |
| `CardLookupServiceTest` | Every COCRDSLC validation and not-found path; xref/account/customer resolution |
| `CardLookupIT` | Full app on H2 seeded from `app/data`: row counts, import rerun, joined lookup for all 50 sample cards, lookup by account, error paths, auth |
| `EbcdicImportIT` | The same app loading the EBCDIC copies |

## Remaining for later phases

Phase numbers follow the migration plan. This project does not yet include the Phase 0 golden-master
recordings or the copybook checksums. INTCALC (CBACT04C) has a separate test-first port in PR #1/#2.

**Online (CICS/BMS)**

| Phase | Programs |
|:---|:---|
| 1 (rest of the pilot) | COACTVWC (account view), COCRDLIC (card list), COTRN00C/COTRN01C (transaction list/view); MQ extension CODATE01, COACCT01 |
| 2a | COCRDUPC, COUSR01C, COUSR02C, COUSR03C; Db2 extension COTRTLIC, COTRTUPC, COBTUPDT |
| 2b | COBIL00C, COTRN02C, COACTUPC |
| 3 | IMS/Db2/MQ authorization: COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C, PAUDBLOD, PAUDBUNL, DBUNLDGS |
| 4 | COSGN00C (replaced here only for the REST slice), COMEN01C, COADM01C, CORPT00C, COUSR00C, and all BMS maps (`app/bms`) |

This slice replaces only COCRDSLC's logic. Its 3270 screen (the `COCRDSL` mapset) stays until Phase 4.

**Batch (programs and JCL)**

| Phase | Programs | JCL |
|:---|:---|:---|
| 2c | CBTRN02C, CBACT04C | POSTTRAN, INTCALC, TRANBKP, COMBTRAN |
| 5 | CBTRN03C, CBSTM03A/CBSTM03B, CBEXPORT, CBIMPORT, CBACT01C–CBACT03C, CBCUS01C, COBSWAIT, CSUTLDTC, CBTRN01C (decision D5), `app/asm/COBDATFT` | TRANREPT, CREASTMT, CBEXPORT, CBIMPORT, READACCT, READCARD, READCUST, READXREF, PRTCATBL, DALYREJS, WAITSTEP, TXT2PDF1, INTRDRJ1/2, FTPJCL |
| 5 (platform jobs, not ported) | (none) | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, TCATBALF, DISCGRP, TRANTYPE, TRANCATG, TRANIDX, DUSRSECJ, DEFCUST, DEFGDGB, DEFGDGD, ESDSRRDS, REPTFILE, OPENFIL, CLOSEFIL, CBADMCDJ. Their dev/test loads are replaced by `sampleDataImportJob`. |
