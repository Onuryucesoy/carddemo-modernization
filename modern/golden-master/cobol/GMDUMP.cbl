      *> Harness only: unloads the account master (in key order) and the
      *> TRANSACT output file to ASCII text for golden-master comparison.
       IDENTIFICATION DIVISION.
       PROGRAM-ID. GMDUMP.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT ACCT-IN     ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS SEQUENTIAL
                  RECORD KEY   IS ACI-ACCT-ID
                  FILE STATUS  IS WS-STATUS.
           SELECT TRAN-IN     ASSIGN TO TRANSACT
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS WS-STATUS.
           SELECT ACCT-OUT    ASSIGN TO ACCTOUT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TRAN-OUT    ASSIGN TO TRANOUT
                  ORGANIZATION IS LINE SEQUENTIAL.
       DATA DIVISION.
       FILE SECTION.
       FD  ACCT-IN.
       01  ACI-REC.
           05 ACI-ACCT-ID          PIC X(11).
           05 FILLER               PIC X(289).
       FD  TRAN-IN.
       01  TRI-REC                 PIC X(350).
       FD  ACCT-OUT.
       01  ACO-REC                 PIC X(300).
       FD  TRAN-OUT.
       01  TRO-REC                 PIC X(350).
       WORKING-STORAGE SECTION.
       01  WS-STATUS               PIC XX.
       01  WS-EOF                  PIC X.
       PROCEDURE DIVISION.
           OPEN INPUT ACCT-IN OUTPUT ACCT-OUT
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ ACCT-IN
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END WRITE ACO-REC FROM ACI-REC
              END-READ
           END-PERFORM
           CLOSE ACCT-IN ACCT-OUT

           OPEN INPUT TRAN-IN OUTPUT TRAN-OUT
           MOVE 'N' TO WS-EOF
           PERFORM UNTIL WS-EOF = 'Y'
              READ TRAN-IN
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END WRITE TRO-REC FROM TRI-REC
              END-READ
           END-PERFORM
           CLOSE TRAN-IN TRAN-OUT
           STOP RUN.
