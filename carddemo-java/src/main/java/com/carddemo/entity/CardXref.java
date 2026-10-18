package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Copybook {@code CVACT03Y}, record {@code CARD-XREF-RECORD (CXACAIX, RECLN 50)}.
 */
@Entity
@Table(name = "card_xref")
public class CardXref {

    /** {@code XREF-CARD-NUM} {@code X(16)} */
    @Id
    @Column(name = "xref_card_num", nullable = false, length = 16)
    private String xrefCardNum;

    /** {@code XREF-CUST-ID} {@code 9(09)} */
    @Column(name = "xref_cust_id")
    private Long xrefCustId;

    /** {@code XREF-ACCT-ID} {@code 9(11)} */
    @Column(name = "xref_acct_id")
    private Long xrefAcctId;

    public String getXrefCardNum() {
        return xrefCardNum;
    }

    public void setXrefCardNum(String xrefCardNum) {
        this.xrefCardNum = xrefCardNum;
    }

    public Long getXrefCustId() {
        return xrefCustId;
    }

    public void setXrefCustId(Long xrefCustId) {
        this.xrefCustId = xrefCustId;
    }

    public Long getXrefAcctId() {
        return xrefAcctId;
    }

    public void setXrefAcctId(Long xrefAcctId) {
        this.xrefAcctId = xrefAcctId;
    }
}
