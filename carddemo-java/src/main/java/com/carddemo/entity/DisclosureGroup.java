package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Copybook {@code CVTRA02Y}, record {@code DIS-GROUP-RECORD (DISCGRP, RECLN 50)}.
 */
@Entity
@Table(name = "disclosure_group")
@IdClass(DisclosureGroupId.class)
public class DisclosureGroup {

    /** {@code DIS-ACCT-GROUP-ID} {@code X(10)} */
    @Id
    @Column(name = "dis_acct_group_id", nullable = false, length = 10)
    private String acctGroupId;

    /** {@code DIS-TRAN-TYPE-CD} {@code X(02)} */
    @Id
    @Column(name = "dis_tran_type_cd", nullable = false, length = 2)
    private String tranTypeCd;

    /** {@code DIS-TRAN-CAT-CD} {@code 9(04)} */
    @Id
    @Column(name = "dis_tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    /** {@code DIS-INT-RATE} {@code S9(04)V99} */
    @Column(name = "dis_int_rate", precision = 6, scale = 2)
    private BigDecimal intRate;

    public String getAcctGroupId() {
        return acctGroupId;
    }

    public void setAcctGroupId(String acctGroupId) {
        this.acctGroupId = acctGroupId;
    }

    public String getTranTypeCd() {
        return tranTypeCd;
    }

    public void setTranTypeCd(String tranTypeCd) {
        this.tranTypeCd = tranTypeCd;
    }

    public Integer getTranCatCd() {
        return tranCatCd;
    }

    public void setTranCatCd(Integer tranCatCd) {
        this.tranCatCd = tranCatCd;
    }

    public BigDecimal getIntRate() {
        return intRate;
    }

    public void setIntRate(BigDecimal intRate) {
        this.intRate = intRate;
    }
}
