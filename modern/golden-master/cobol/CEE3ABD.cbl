      *> Harness only: stand-in for the LE abend service. Reports the user
      *> abend code on stderr and ends the run unit with RC 16.
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  WS-CODE                 PIC 9(4).
       LINKAGE SECTION.
       01  LK-ABCODE               PIC S9(9) BINARY.
       01  LK-TIMING               PIC S9(9) BINARY.
       PROCEDURE DIVISION USING LK-ABCODE LK-TIMING.
           MOVE LK-ABCODE TO WS-CODE
           DISPLAY 'ABEND U' WS-CODE UPON SYSERR
           MOVE 16 TO RETURN-CODE
           STOP RUN.
