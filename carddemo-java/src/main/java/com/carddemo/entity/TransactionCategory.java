package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Copybook {@code CVTRA04Y}, record {@code TRAN-CAT-RECORD (TRANCATG, RECLN 60)}.
 */
@Entity
@Table(name = "transaction_category")
@IdClass(TransactionCategoryId.class)
public class TransactionCategory {

    /** {@code TRAN-TYPE-CD} {@code X(02)} */
    @Id
    @Column(name = "tran_type_cd", nullable = false, length = 2)
    private String tranTypeCd;

    /** {@code TRAN-CAT-CD} {@code 9(04)} */
    @Id
    @Column(name = "tran_cat_cd", nullable = false)
    private Integer tranCatCd;

    /** {@code TRAN-CAT-TYPE-DESC} {@code X(50)} */
    @Column(name = "tran_cat_type_desc", length = 50)
    private String tranCatTypeDesc;

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

    public String getTranCatTypeDesc() {
        return tranCatTypeDesc;
    }

    public void setTranCatTypeDesc(String tranCatTypeDesc) {
        this.tranCatTypeDesc = tranCatTypeDesc;
    }
}
