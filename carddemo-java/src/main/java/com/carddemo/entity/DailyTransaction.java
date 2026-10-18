package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Copybook {@code CVTRA06Y}, record {@code DALYTRAN-RECORD (DALYTRAN, RECLN 350)}.
 */
@Entity
@Table(name = "daily_transaction")
public class DailyTransaction {

    /** {@code DALYTRAN-ID} {@code X(16)} */
    @Id
    @Column(name = "dalytran_id", nullable = false, length = 16)
    private String tranId;

    /** {@code DALYTRAN-TYPE-CD} {@code X(02)} */
    @Column(name = "dalytran_type_cd", length = 2)
    private String typeCd;

    /** {@code DALYTRAN-CAT-CD} {@code 9(04)} */
    @Column(name = "dalytran_cat_cd")
    private Integer catCd;

    /** {@code DALYTRAN-SOURCE} {@code X(10)} */
    @Column(name = "dalytran_source", length = 10)
    private String source;

    /** {@code DALYTRAN-DESC} {@code X(100)} */
    @Column(name = "dalytran_desc", length = 100)
    private String description;

    /** {@code DALYTRAN-AMT} {@code S9(09)V99} */
    @Column(name = "dalytran_amt", precision = 11, scale = 2)
    private BigDecimal amount;

    /** {@code DALYTRAN-MERCHANT-ID} {@code 9(09)} */
    @Column(name = "dalytran_merchant_id")
    private Long merchantId;

    /** {@code DALYTRAN-MERCHANT-NAME} {@code X(50)} */
    @Column(name = "dalytran_merchant_name", length = 50)
    private String merchantName;

    /** {@code DALYTRAN-MERCHANT-CITY} {@code X(50)} */
    @Column(name = "dalytran_merchant_city", length = 50)
    private String merchantCity;

    /** {@code DALYTRAN-MERCHANT-ZIP} {@code X(10)} */
    @Column(name = "dalytran_merchant_zip", length = 10)
    private String merchantZip;

    /** {@code DALYTRAN-CARD-NUM} {@code X(16)} */
    @Column(name = "dalytran_card_num", length = 16)
    private String cardNum;

    /** {@code DALYTRAN-ORIG-TS} {@code X(26)} */
    @Column(name = "dalytran_orig_ts")
    private LocalDateTime origTs;

    /** {@code DALYTRAN-PROC-TS} {@code X(26)} */
    @Column(name = "dalytran_proc_ts")
    private LocalDateTime procTs;

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTypeCd() {
        return typeCd;
    }

    public void setTypeCd(String typeCd) {
        this.typeCd = typeCd;
    }

    public Integer getCatCd() {
        return catCd;
    }

    public void setCatCd(Integer catCd) {
        this.catCd = catCd;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    public String getMerchantZip() {
        return merchantZip;
    }

    public void setMerchantZip(String merchantZip) {
        this.merchantZip = merchantZip;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public LocalDateTime getOrigTs() {
        return origTs;
    }

    public void setOrigTs(LocalDateTime origTs) {
        this.origTs = origTs;
    }

    public LocalDateTime getProcTs() {
        return procTs;
    }

    public void setProcTs(LocalDateTime procTs) {
        this.procTs = procTs;
    }
}
