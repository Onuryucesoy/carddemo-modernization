package com.carddemo.batch;

/**
 * The sample unload files under {@code app/data}, with their ASCII and EBCDIC file names.
 * {@code USRSEC} and {@code EXPORT} only ship as EBCDIC.
 */
public enum SampleDataset {
    TRANTYPE("trantype.txt", "AWS.M2.CARDDEMO.TRANTYPE.PS"),
    TRANCATG("trancatg.txt", "AWS.M2.CARDDEMO.TRANCATG.PS"),
    DISCGRP("discgrp.txt", "AWS.M2.CARDDEMO.DISCGRP.PS"),
    CUSTDATA("custdata.txt", "AWS.M2.CARDDEMO.CUSTDATA.PS"),
    ACCTDATA("acctdata.txt", "AWS.M2.CARDDEMO.ACCTDATA.PS"),
    CARDDATA("carddata.txt", "AWS.M2.CARDDEMO.CARDDATA.PS"),
    CARDXREF("cardxref.txt", "AWS.M2.CARDDEMO.CARDXREF.PS"),
    TCATBAL("tcatbal.txt", "AWS.M2.CARDDEMO.TCATBALF.PS"),
    DALYTRAN("dailytran.txt", "AWS.M2.CARDDEMO.DALYTRAN.PS"),
    USRSEC(null, "AWS.M2.CARDDEMO.USRSEC.PS"),
    EXPORT(null, "AWS.M2.CARDDEMO.EXPORT.DATA.PS");

    private final String asciiFile;
    private final String ebcdicFile;

    SampleDataset(String asciiFile, String ebcdicFile) {
        this.asciiFile = asciiFile;
        this.ebcdicFile = ebcdicFile;
    }

    public String asciiFile() {
        return asciiFile;
    }

    public String ebcdicFile() {
        return ebcdicFile;
    }

    public boolean hasAscii() {
        return asciiFile != null;
    }
}
