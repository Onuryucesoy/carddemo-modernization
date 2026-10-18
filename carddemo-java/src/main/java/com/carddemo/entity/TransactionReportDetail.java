package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Copybook {@code CVTRA07Y}, record {@code TRANSACTION-DETAIL-REPORT} (CBTRN03C print line).
 * The header/total records in the copybook are print constants and are not persisted; this
 * table is the target for the Phase 3 transaction report and is not seeded from sample data.
 */
@Entity
@Table(name = "transaction_report_detail")
public class TransactionReportDetail {

    /** {@code TRAN-REPORT-TRANS-ID} {@code X(16)} */
    @Id
    @Column(name = "tran_report_trans_id", nullable = false, length = 16)
    private String transId;

    /** {@code TRAN-REPORT-ACCOUNT-ID} {@code X(11)} */
    @Column(name = "tran_report_account_id", length = 11)
    private String accountId;

    /** {@code TRAN-REPORT-TYPE-CD} {@code X(02)} */
    @Column(name = "tran_report_type_cd", length = 2)
    private String typeCd;

    /** {@code TRAN-REPORT-TYPE-DESC} {@code X(15)} */
    @Column(name = "tran_report_type_desc", length = 15)
    private String typeDesc;

    /** {@code TRAN-REPORT-CAT-CD} {@code 9(04)} */
    @Column(name = "tran_report_cat_cd")
    private Integer catCd;

    /** {@code TRAN-REPORT-CAT-DESC} {@code X(29)} */
    @Column(name = "tran_report_cat_desc", length = 29)
    private String catDesc;

    /** {@code TRAN-REPORT-SOURCE} {@code X(10)} */
    @Column(name = "tran_report_source", length = 10)
    private String source;

    /** {@code TRAN-REPORT-AMT} {@code -ZZZ,ZZZ,ZZZ.ZZ} (edited; stored as its numeric value) */
    @Column(name = "tran_report_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public void setCatCd(Integer catCd) {
        this.catCd = catCd;
    }

    public String getCatDesc() {
        return catDesc;
    }

    public void setCatDesc(String catDesc) {
        this.catDesc = catDesc;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
