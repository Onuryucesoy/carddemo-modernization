      *> Harness only: stands in for the z/OS EXEC PGM=CBACT04C,PARM='...'
      *> step. Builds the PARM block (halfword length + data) and calls the
      *> unmodified legacy program.
       IDENTIFICATION DIVISION.
       PROGRAM-ID. GMDRIVER.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-CMDLINE              PIC X(100).
       01  WS-PARMS.
           05  WS-PARM-LENGTH      PIC S9(4) COMP.
           05  WS-PARM-DATA        PIC X(100).
       PROCEDURE DIVISION.
           ACCEPT WS-CMDLINE FROM COMMAND-LINE
           MOVE WS-CMDLINE TO WS-PARM-DATA
           MOVE FUNCTION LENGTH(FUNCTION TRIM(WS-CMDLINE))
             TO WS-PARM-LENGTH
           CALL 'CBACT04C' USING WS-PARMS
           STOP RUN.
