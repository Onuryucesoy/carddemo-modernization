"""Generates the 'month-end' fixture: non-zero balances that exercise every
interest rule in CBACT04C (truncation, negative balances, DEFAULT disclosure
group fallback, zero-rate groups, multi-category accounts, last account)."""
from decimal import Decimal
from pathlib import Path

POS = "{ABCDEFGHI"
NEG = "}JKLMNOPQR"


def zoned(value, digits, scale):
    n = int((Decimal(value) * (10 ** scale)).to_integral_value())
    s = str(abs(n)).rjust(digits, "0")
    last = (NEG if n < 0 else POS)[int(s[-1])]
    return s[:-1] + last


def account(acct_id, bal, group, cyc_credit, cyc_debit):
    rec = (str(acct_id).rjust(11, "0") + "Y" + zoned(bal, 12, 2)
           + zoned("5000", 12, 2) + zoned("1000", 12, 2)
           + "2020-01-012030-01-012030-01-01"
           + zoned(cyc_credit, 12, 2) + zoned(cyc_debit, 12, 2)
           + "60601     " + group.ljust(10))
    return rec.ljust(300)


def xref(card, cust, acct_id):
    return (card + str(cust).rjust(9, "0") + str(acct_id).rjust(11, "0")).ljust(50)


def tcatbal(acct_id, type_cd, cat_cd, bal):
    return (str(acct_id).rjust(11, "0") + type_cd + cat_cd + zoned(bal, 11, 2)).ljust(50)


accounts = [
    account(1, "1940.00", "A000000000", "120.00", "300.00"),
    account(2, "-250.00", "A000000000", "0", "75.50"),
    account(3, "800.00", "PREMIUM", "10.00", "10.00"),
    account(4, "9999.99", "ZEROAPR", "1.00", "2.00"),
    account(5, "1200.00", "A000000000", "50.00", "60.00"),
]
xrefs = [xref(f"40000000000000{i:02d}", 100 + i, i) for i in range(1, 6)]
balances = [
    tcatbal(1, "01", "0001", "1000.00"),   # 15.00% -> 12.50
    tcatbal(1, "01", "0002", "333.33"),    # 25.00% -> 6.944375 truncated to 6.94
    tcatbal(1, "01", "0003", "0.07"),      # 25.00% -> 0.00145.. truncated to 0.00
    tcatbal(2, "01", "0004", "-500.00"),   # 25.00% -> -10.41666.. truncated to -10.41
    tcatbal(2, "02", "0001", "700.00"),    # 0.00% -> no transaction
    tcatbal(3, "01", "0001", "2500.00"),   # group PREMIUM missing -> DEFAULT 15.00%
    tcatbal(4, "01", "0001", "9999.99"),   # ZEROAPR -> no transaction
    tcatbal(5, "01", "0001", "1200.00"),   # last account in file
]

here = Path(__file__).parent / "month-end"
here.mkdir(exist_ok=True)
(here / "acctdata.txt").write_text("\n".join(accounts) + "\n")
(here / "cardxref.txt").write_text("\n".join(xrefs) + "\n")
(here / "tcatbal.txt").write_text("\n".join(balances) + "\n")
(here / "discgrp.txt").write_text((Path(__file__).parent / "seed" / "discgrp.txt").read_text())
