package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Copybook {@code CVTRA03Y}, record {@code TRAN-TYPE-RECORD (TRANTYPE, RECLN 60)}.
 */
@Entity
@Table(name = "transaction_type")
public class TransactionType {

    /** {@code TRAN-TYPE} {@code X(02)} */
    @Id
    @Column(name = "tran_type", nullable = false, length = 2)
    private String tranType;

    /** {@code TRAN-TYPE-DESC} {@code X(50)} */
    @Column(name = "tran_type_desc", length = 50)
    private String tranTypeDesc;

    public String getTranType() {
        return tranType;
    }

    public void setTranType(String tranType) {
        this.tranType = tranType;
    }

    public String getTranTypeDesc() {
        return tranTypeDesc;
    }

    public void setTranTypeDesc(String tranTypeDesc) {
        this.tranTypeDesc = tranTypeDesc;
    }
}
