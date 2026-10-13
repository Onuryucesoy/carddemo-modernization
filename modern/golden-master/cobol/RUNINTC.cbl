      *****************************************************************
      * Test harness: stands in for JCL step INTCALC/STEP15 and calls
      * the unmodified CBACT04C with PARM='<date>' (from env PARMDATE).
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. RUNINTC.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  EXTERNAL-PARMS.
           05  PARM-LENGTH         PIC S9(04) COMP VALUE 10.
           05  PARM-DATE           PIC X(10).
       PROCEDURE DIVISION.
           ACCEPT PARM-DATE FROM ENVIRONMENT 'PARMDATE'
           CALL 'CBACT04C' USING EXTERNAL-PARMS
           STOP RUN.
