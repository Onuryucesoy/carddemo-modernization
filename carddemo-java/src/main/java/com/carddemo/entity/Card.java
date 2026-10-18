package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Copybook {@code CVACT02Y}, record {@code CARD-RECORD (CARDDAT, RECLN 150)}.
 */
@Entity
@Table(name = "card")
public class Card {

    /** {@code CARD-NUM} {@code X(16)} */
    @Id
    @Column(name = "card_num", nullable = false, length = 16)
    private String cardNum;

    /** {@code CARD-ACCT-ID} {@code 9(11)} */
    @Column(name = "card_acct_id")
    private Long acctId;

    /** {@code CARD-CVV-CD} {@code 9(03)} */
    @Column(name = "card_cvv_cd")
    private Integer cvvCd;

    /** {@code CARD-EMBOSSED-NAME} {@code X(50)} */
    @Column(name = "card_embossed_name", length = 50)
    private String embossedName;

    /** {@code CARD-EXPIRAION-DATE} {@code X(10)} */
    @Column(name = "card_expiration_date")
    private LocalDate expirationDate;

    /** {@code CARD-ACTIVE-STATUS} {@code X(01)} */
    @Column(name = "card_active_status", length = 1)
    private Character activeStatus;

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public Integer getCvvCd() {
        return cvvCd;
    }

    public void setCvvCd(Integer cvvCd) {
        this.cvvCd = cvvCd;
    }

    public String getEmbossedName() {
        return embossedName;
    }

    public void setEmbossedName(String embossedName) {
        this.embossedName = embossedName;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Character getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Character activeStatus) {
        this.activeStatus = activeStatus;
    }
}
