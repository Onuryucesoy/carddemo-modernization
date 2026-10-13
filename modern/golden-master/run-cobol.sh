#!/usr/bin/env bash
# Runs the UNMODIFIED legacy program app/cbl/CBACT04C.cbl (JCL app/jcl/INTCALC.jcl)
# under GnuCOBOL against one fixture scenario and records everything it produces.
#
#   usage: run-cobol.sh <scenario> [output-dir]
#   default output-dir: expected/<scenario>
#
# Recorded files: sysout.txt (DISPLAY output), transact.txt (TRANSACT records),
# acctdata.txt (account master after the run, key order), result.txt (RC or abend).
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
SCENARIO="${1:?usage: run-cobol.sh <scenario> [output-dir]}"
FIXTURES="$HERE/fixtures/$SCENARIO"
OUT="${2:-$HERE/expected/$SCENARIO}"
BUILD="$HERE/build"

# Same values on every run so the recording is deterministic.
PARM="${PARM:-2022071800}"
export COB_CURRENT_DATE="${COB_CURRENT_DATE:-2022/07/18 23:30:00.00}"

[ -d "$FIXTURES" ] || { echo "no such scenario: $FIXTURES" >&2; exit 2; }

mkdir -p "$BUILD"
COBFLAGS=(-std=ibm -fsign=EBCDIC)
cobc -m "${COBFLAGS[@]}" -I "$REPO/app/cpy" -o "$BUILD/CBACT04C.so" "$REPO/app/cbl/CBACT04C.cbl"
cobc -m "${COBFLAGS[@]}" -o "$BUILD/CEE3ABD.so" "$HERE/cobol/CEE3ABD.cbl"
cobc -x "${COBFLAGS[@]}" -o "$BUILD/gmdriver" "$HERE/cobol/GMDRIVER.cbl"
cobc -x "${COBFLAGS[@]}" -o "$BUILD/gmload"   "$HERE/cobol/GMLOAD.cbl"
cobc -x "${COBFLAGS[@]}" -o "$BUILD/gmdump"   "$HERE/cobol/GMDUMP.cbl"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
for f in tcatbal cardxref acctdata discgrp; do
  tr -d '\r' < "$FIXTURES/$f.txt" > "$WORK/$f.txt"
done

export COB_LIBRARY_PATH="$BUILD"
export COB_LS_FIXED=TRUE          # keep full fixed-length records in text dumps
export TCATBALT="$WORK/tcatbal.txt" XREFTXT="$WORK/cardxref.txt"
export ACCTTXT="$WORK/acctdata.txt" DISCGRPT="$WORK/discgrp.txt"
export TCATBALF="$WORK/tcatbal.dat" XREFFILE="$WORK/cardxref.dat"
export ACCTFILE="$WORK/acctdata.dat" DISCGRP="$WORK/discgrp.dat"
export TRANSACT="$WORK/transact.dat"
export ACCTOUT="$WORK/acctdata.out" TRANOUT="$WORK/transact.out"

(cd "$WORK" && "$BUILD/gmload" >/dev/null)

set +e
(cd "$WORK" && "$BUILD/gmdriver" "$PARM" > "$WORK/sysout.txt" 2> "$WORK/stderr.txt")
RC=$?
set -e

[ -f "$TRANSACT" ] || : > "$TRANSACT"
(cd "$WORK" && "$BUILD/gmdump")

mkdir -p "$OUT"
cp "$WORK/sysout.txt" "$OUT/sysout.txt"
cp "$WORK/transact.out" "$OUT/transact.txt"
cp "$WORK/acctdata.out" "$OUT/acctdata.txt"
if grep -q '^ABEND U' "$WORK/stderr.txt"; then
  grep '^ABEND U' "$WORK/stderr.txt" > "$OUT/result.txt"
else
  printf 'RC=%04d\n' "$RC" > "$OUT/result.txt"
fi
if grep -v -e '^ABEND U' -e '^libcob: warning: implicit CLOSE' "$WORK/stderr.txt" | grep -q .; then
  echo "unexpected stderr from CBACT04C:" >&2; cat "$WORK/stderr.txt" >&2; exit 1
fi

echo "[$SCENARIO] $(cat "$OUT/result.txt"), $(wc -l < "$OUT/transact.txt") transactions, $(wc -l < "$OUT/sysout.txt") sysout lines -> ${OUT#$REPO/}"
