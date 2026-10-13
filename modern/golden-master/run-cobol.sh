#!/usr/bin/env bash
# Runs the unmodified legacy CBACT04C (INTCALC) under GnuCOBOL against a
# fixture scenario and records its outputs as the golden master.
# Usage: ./run-cobol.sh <scenario> [parm-date]
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/../.." && pwd)"
SCENARIO="$1"
PARM="${2:-2022071800}"
FIX="$HERE/fixtures/$SCENARIO"
OUT="$HERE/expected/$SCENARIO"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

COBFLAGS=(-std=ibm -fsign=EBCDIC -I "$ROOT/app/cpy")
cobc -x "${COBFLAGS[@]}" -o "$WORK/loaddata" "$HERE/cobol/LOADDATA.cbl"
cobc -x "${COBFLAGS[@]}" -o "$WORK/dumpdata" "$HERE/cobol/DUMPDATA.cbl"
cobc -x "${COBFLAGS[@]}" -o "$WORK/runintc" "$HERE/cobol/RUNINTC.cbl" \
  "$ROOT/app/cbl/CBACT04C.cbl" "$HERE/cobol/CEE3ABD.cbl"

export TCATBALT="$FIX/tcatbal.txt" XREFT="$FIX/cardxref.txt" \
       ACCTT="$FIX/acctdata.txt" DISCGRPT="$FIX/discgrp.txt" \
       TCATBALF="$WORK/tcatbal.idx" XREFFILE="$WORK/xref.idx" \
       ACCTFILE="$WORK/acct.idx" DISCGRP="$WORK/discgrp.idx" \
       TRANSACT="$WORK/transact.dat" PARMDATE="$PARM" \
       ACCTOUT="$WORK/acctdata.out.txt" TRANOUT="$WORK/transact.out.txt"
"$WORK/loaddata"
"$WORK/runintc" > "$WORK/sysout.txt"
"$WORK/dumpdata"

mkdir -p "$OUT"
cp "$WORK/acctdata.out.txt" "$OUT/acctdata.txt"
cp "$WORK/transact.out.txt" "$OUT/transact.txt"
cp "$WORK/sysout.txt" "$OUT/sysout.txt"
echo "golden master written to $OUT"
