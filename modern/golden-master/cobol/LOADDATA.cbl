      *****************************************************************
      * Test harness: loads ASCII line-sequential fixtures into the
      * indexed (VSAM KSDS equivalent) files read by CBACT04C.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADDATA.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-TCATBAL ASSIGN TO TCATBALT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT IN-XREF    ASSIGN TO XREFT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT IN-ACCT    ASSIGN TO ACCTT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT IN-DISCGRP ASSIGN TO DISCGRPT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TCATBAL-FILE ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-TRAN-CAT-KEY.
           SELECT XREF-FILE ASSIGN TO XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-XREF-CARD-NUM
                  ALTERNATE RECORD KEY IS FD-XREF-ACCT-ID.
           SELECT ACCOUNT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-ACCT-ID.
           SELECT DISCGRP-FILE ASSIGN TO DISCGRP
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-DISCGRP-KEY.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-TCATBAL.
       01  IN-TCATBAL-REC              PIC X(50).
       FD  IN-XREF.
       01  IN-XREF-REC                 PIC X(50).
       FD  IN-ACCT.
       01  IN-ACCT-REC                 PIC X(300).
       FD  IN-DISCGRP.
       01  IN-DISCGRP-REC              PIC X(50).
       FD  TCATBAL-FILE.
       01  FD-TRAN-CAT-BAL-RECORD.
           05 FD-TRAN-CAT-KEY          PIC X(17).
           05 FILLER                   PIC X(33).
       FD  XREF-FILE.
       01  FD-XREFFILE-REC.
           05 FD-XREF-CARD-NUM         PIC X(16).
           05 FILLER                   PIC X(09).
           05 FD-XREF-ACCT-ID          PIC 9(11).
           05 FILLER                   PIC X(14).
       FD  ACCOUNT-FILE.
       01  FD-ACCTFILE-REC.
           05 FD-ACCT-ID               PIC 9(11).
           05 FILLER                   PIC X(289).
       FD  DISCGRP-FILE.
       01  FD-DISCGRP-REC.
           05 FD-DISCGRP-KEY           PIC X(16).
           05 FILLER                   PIC X(34).
       WORKING-STORAGE SECTION.
       01  WS-EOF                      PIC X VALUE 'N'.
       PROCEDURE DIVISION.
           OPEN INPUT IN-TCATBAL OUTPUT TCATBAL-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
               READ IN-TCATBAL AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE FD-TRAN-CAT-BAL-RECORD
                          FROM IN-TCATBAL-REC
               END-READ
           END-PERFORM
           CLOSE IN-TCATBAL TCATBAL-FILE
           OPEN INPUT IN-XREF OUTPUT XREF-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
               READ IN-XREF AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE FD-XREFFILE-REC FROM IN-XREF-REC
               END-READ
           END-PERFORM
           CLOSE IN-XREF XREF-FILE
           OPEN INPUT IN-ACCT OUTPUT ACCOUNT-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
               READ IN-ACCT AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE FD-ACCTFILE-REC FROM IN-ACCT-REC
               END-READ
           END-PERFORM
           CLOSE IN-ACCT ACCOUNT-FILE
           OPEN INPUT IN-DISCGRP OUTPUT DISCGRP-FILE
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
               READ IN-DISCGRP AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE FD-DISCGRP-REC FROM IN-DISCGRP-REC
               END-READ
           END-PERFORM
           CLOSE IN-DISCGRP DISCGRP-FILE
           STOP RUN.
