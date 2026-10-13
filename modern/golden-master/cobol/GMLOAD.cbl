      *> Harness only: loads ASCII fixture files into BDB indexed files
      *> whose keys match the SELECTs in CBACT04C (the VSAM KSDS layout).
       IDENTIFICATION DIVISION.
       PROGRAM-ID. GMLOAD.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TCATBAL-IN  ASSIGN TO TCATBALT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT XREF-IN     ASSIGN TO XREFTXT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT ACCT-IN     ASSIGN TO ACCTTXT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT DISCGRP-IN  ASSIGN TO DISCGRPT
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TCATBAL-OUT ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS TCO-KEY
                  FILE STATUS  IS WS-STATUS.
           SELECT XREF-OUT    ASSIGN TO XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS XRO-CARD-NUM
                  ALTERNATE RECORD KEY IS XRO-ACCT-ID
                  FILE STATUS  IS WS-STATUS.
           SELECT ACCT-OUT    ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS ACO-ACCT-ID
                  FILE STATUS  IS WS-STATUS.
           SELECT DISCGRP-OUT ASSIGN TO DISCGRP
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS DGO-KEY
                  FILE STATUS  IS WS-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  TCATBAL-IN.
       01  TCI-REC                 PIC X(50).
       FD  XREF-IN.
       01  XRI-REC                 PIC X(50).
       FD  ACCT-IN.
       01  ACI-REC                 PIC X(300).
       FD  DISCGRP-IN.
       01  DGI-REC                 PIC X(50).
       FD  TCATBAL-OUT.
       01  TCO-REC.
           05 TCO-KEY              PIC X(17).
           05 FILLER               PIC X(33).
       FD  XREF-OUT.
       01  XRO-REC.
           05 XRO-CARD-NUM         PIC X(16).
           05 FILLER               PIC X(09).
           05 XRO-ACCT-ID          PIC X(11).
           05 FILLER               PIC X(14).
       FD  ACCT-OUT.
       01  ACO-REC.
           05 ACO-ACCT-ID          PIC X(11).
           05 FILLER               PIC X(289).
       FD  DISCGRP-OUT.
       01  DGO-REC.
           05 DGO-KEY              PIC X(16).
           05 FILLER               PIC X(34).
       WORKING-STORAGE SECTION.
       01  WS-STATUS               PIC XX.
       01  WS-EOF                  PIC X.
       01  WS-COUNT                PIC 9(6).
       PROCEDURE DIVISION.
           OPEN INPUT TCATBAL-IN OUTPUT TCATBAL-OUT
           MOVE 'N' TO WS-EOF  MOVE 0 TO WS-COUNT
           PERFORM UNTIL WS-EOF = 'Y'
              READ TCATBAL-IN
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END
                    WRITE TCO-REC FROM TCI-REC
                    PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE TCATBAL-IN TCATBAL-OUT
           DISPLAY 'TCATBALF loaded: ' WS-COUNT

           OPEN INPUT XREF-IN OUTPUT XREF-OUT
           MOVE 'N' TO WS-EOF  MOVE 0 TO WS-COUNT
           PERFORM UNTIL WS-EOF = 'Y'
              READ XREF-IN
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END
                    WRITE XRO-REC FROM XRI-REC
                    PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE XREF-IN XREF-OUT
           DISPLAY 'XREFFILE loaded: ' WS-COUNT

           OPEN INPUT ACCT-IN OUTPUT ACCT-OUT
           MOVE 'N' TO WS-EOF  MOVE 0 TO WS-COUNT
           PERFORM UNTIL WS-EOF = 'Y'
              READ ACCT-IN
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END
                    WRITE ACO-REC FROM ACI-REC
                    PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE ACCT-IN ACCT-OUT
           DISPLAY 'ACCTFILE loaded: ' WS-COUNT

           OPEN INPUT DISCGRP-IN OUTPUT DISCGRP-OUT
           MOVE 'N' TO WS-EOF  MOVE 0 TO WS-COUNT
           PERFORM UNTIL WS-EOF = 'Y'
              READ DISCGRP-IN
                 AT END MOVE 'Y' TO WS-EOF
                 NOT AT END
                    WRITE DGO-REC FROM DGI-REC
                    PERFORM CHECK-STATUS
              END-READ
           END-PERFORM
           CLOSE DISCGRP-IN DISCGRP-OUT
           DISPLAY 'DISCGRP  loaded: ' WS-COUNT
           STOP RUN.

       CHECK-STATUS.
           IF WS-STATUS NOT = '00'
              DISPLAY 'LOAD FAILED, FILE STATUS ' WS-STATUS UPON SYSERR
              MOVE 8 TO RETURN-CODE
              STOP RUN
           END-IF
           ADD 1 TO WS-COUNT.
