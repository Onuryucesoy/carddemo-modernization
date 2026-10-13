      *****************************************************************
      * Test harness: dumps CBACT04C outputs (account master after
      * REWRITE, generated interest transactions) to text files.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. DUMPDATA.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCOUNT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS FD-ACCT-ID.
           SELECT TRANSACT-FILE ASSIGN TO TRANSACT
                  ORGANIZATION IS SEQUENTIAL.
           SELECT OUT-ACCT ASSIGN TO ACCTOUT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT OUT-TRAN ASSIGN TO TRANOUT
                  ORGANIZATION IS LINE SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCOUNT-FILE.
       01  FD-ACCTFILE-REC.
           05 FD-ACCT-ID               PIC 9(11).
           05 FILLER                   PIC X(289).
       FD  TRANSACT-FILE.
       01  FD-TRANFILE-REC             PIC X(350).
       FD  OUT-ACCT.
       01  OUT-ACCT-REC                PIC X(300).
       FD  OUT-TRAN.
       01  OUT-TRAN-REC                PIC X(350).
       WORKING-STORAGE SECTION.
       01  WS-EOF                      PIC X VALUE 'N'.
       PROCEDURE DIVISION.
           OPEN INPUT ACCOUNT-FILE OUTPUT OUT-ACCT
           PERFORM UNTIL WS-EOF = 'Y'
               READ ACCOUNT-FILE AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE OUT-ACCT-REC FROM FD-ACCTFILE-REC
               END-READ
           END-PERFORM
           CLOSE ACCOUNT-FILE OUT-ACCT
           MOVE 'N' TO WS-EOF
           OPEN INPUT TRANSACT-FILE OUTPUT OUT-TRAN
           PERFORM UNTIL WS-EOF = 'Y'
               READ TRANSACT-FILE AT END MOVE 'Y' TO WS-EOF
               NOT AT END WRITE OUT-TRAN-REC FROM FD-TRANFILE-REC
               END-READ
           END-PERFORM
           CLOSE TRANSACT-FILE OUT-TRAN
           STOP RUN.
