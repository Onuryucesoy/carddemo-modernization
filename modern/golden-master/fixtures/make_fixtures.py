#!/usr/bin/env python3
"""Generates the hand-designed fixture scenarios (seed/ is the shipped app/data/ASCII data).

Record layouts follow the copybooks: CVTRA01Y (TCATBAL), CVACT03Y (XREF),
CVACT01Y (ACCOUNT), CVTRA02Y (DISCGRP). Signed numbers use zoned decimal with
an overpunched trailing sign, exactly like the shipped ASCII data.
"""
from decimal import Decimal
from pathlib import Path

HERE = Path(__file__).resolve().parent
SEED_DISCGRP = (HERE / "seed" / "discgrp.txt").read_text().replace("\r", "")

POS = "{ABCDEFGHI"
NEG = "}JKLMNOPQR"


def zoned(value, int_digits, dec_digits=2):
    d = Decimal(value)
    digits = f"{abs(d) * 10**dec_digits:0{int_digits + dec_digits}.0f}"
    assert len(digits) == int_digits + dec_digits, (value, digits)
    return digits[:-1] + (NEG if d < 0 else POS)[int(digits[-1])]


def tcatbal(acct, type_cd, cat_cd, bal):
    return f"{acct:011d}{type_cd}{cat_cd:04d}{zoned(bal, 9)}" + "0" * 22


def xref(card, cust, acct):
    return f"{card}{cust:09d}{acct:011d}"


def account(acct, curr_bal, cyc_credit, cyc_debit, group):
    rec = (f"{acct:011d}Y{zoned(curr_bal, 10)}{zoned('5000', 10)}{zoned('1500', 10)}"
           f"2019-01-152027-01-152025-01-15{zoned(cyc_credit, 10)}{zoned(cyc_debit, 10)}"
           f"{'10001':<10}{group:<10}")
    return rec.ljust(300)


def disc(group, type_cd, cat_cd, rate):
    return f"{group:<10}{type_cd}{cat_cd:04d}{zoned(rate, 4)}" + "0" * 28


def write(scenario, tcat, xrefs, accts, discgrp=SEED_DISCGRP):
    d = HERE / scenario
    d.mkdir(exist_ok=True)
    (d / "tcatbal.txt").write_text("\n".join(tcat) + "\n")
    (d / "cardxref.txt").write_text("\n".join(xrefs) + "\n")
    (d / "acctdata.txt").write_text("\n".join(accts) + "\n")
    (d / "discgrp.txt").write_text(discgrp if discgrp.endswith("\n") else discgrp + "\n")


# month-end: realistic balances across the business rules.
#  11  own group A000000000, two rated categories + one 0% category, cycle totals reset
#  12  group GOLDCARD has no DISCGRP rows -> DEFAULT fallback; negative balance (truncation toward zero)
#  13  group ZEROAPR -> rate 0, no interest transaction, account still rewritten
#  14  sub-cent interest (0.00 tx still written) + large balance in category 04
#  15  last account in the file: interest tx written, balance never posted (legacy behaviour)
#  16  no TCATBAL rows -> untouched
write("month-end",
      [tcatbal(11, "01", 1, "1234.56"), tcatbal(11, "01", 2, "789.01"), tcatbal(11, "02", 1, "500.00"),
       tcatbal(12, "01", 1, "-833.33"), tcatbal(12, "01", 3, "99.99"),
       tcatbal(13, "01", 1, "5000.00"),
       tcatbal(14, "01", 4, "0.01"), tcatbal(14, "04", 1, "10000.00"),
       tcatbal(15, "01", 1, "2000.00")],
      [xref("4111111111110011", 101, 11), xref("4111111111110012", 102, 12),
       xref("4111111111110013", 103, 13), xref("4111111111110014", 104, 14),
       xref("4111111111110015", 105, 15), xref("4111111111110016", 106, 16)],
      [account(11, "2523.57", "100.00", "250.00", "A000000000"),
       account(12, "-733.34", "833.33", "99.99", "GOLDCARD"),
       account(13, "5000.00", "0", "5000.00", "ZEROAPR"),
       account(14, "10000.01", "0", "0", "A000000000"),
       account(15, "2000.00", "0", "2000.00", "A000000000"),
       account(16, "42.00", "1.00", "2.00", "A000000000")])

# overflow: rates/balances big enough to overflow the PIC S9(09)V99 interest fields.
write("overflow",
      [tcatbal(31, "01", 1, "999999999.99"), tcatbal(31, "01", 2, "-999999999.99"),
       tcatbal(31, "01", 3, "999999999.99"), tcatbal(32, "01", 1, "1.00")],
      [xref("4111111111110031", 131, 31), xref("4111111111110032", 132, 32)],
      [account(31, "9999999999.99", "0", "0", "BIGRATE"),
       account(32, "0", "0", "0", "BIGRATE")],
      "\n".join([disc("BIGRATE", "01", 1, "9999.99"), disc("BIGRATE", "01", 2, "9999.99"),
                 disc("BIGRATE", "01", 3, "1500.00"), disc("DEFAULT", "01", 1, "15.00")]))

# abend: second category has no DISCGRP row for the account group nor DEFAULT -> U999.
write("abend-missing-default",
      [tcatbal(21, "01", 1, "100.00"), tcatbal(22, "01", 1, "50.00"), tcatbal(22, "09", 9, "50.00")],
      [xref("4111111111110021", 121, 21), xref("4111111111110022", 122, 22)],
      [account(21, "100.00", "10.00", "20.00", ""),
       account(22, "100.00", "10.00", "20.00", "")])

# abend: TCATBAL row for an account that is not on the account master.
write("abend-missing-account",
      [tcatbal(41, "01", 1, "100.00"), tcatbal(42, "01", 1, "100.00")],
      [xref("4111111111110041", 141, 41), xref("4111111111110042", 142, 42)],
      [account(41, "100.00", "1.00", "2.00", "A000000000")])

# abend: account exists but has no card cross-reference.
write("abend-missing-xref",
      [tcatbal(51, "01", 1, "100.00"), tcatbal(52, "01", 1, "100.00")],
      [xref("4111111111110051", 151, 51)],
      [account(51, "100.00", "1.00", "2.00", "A000000000"),
       account(52, "100.00", "1.00", "2.00", "A000000000")])
