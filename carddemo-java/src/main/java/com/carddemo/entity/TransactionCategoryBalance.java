package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Copybook {@code CVTRA01Y}, record {@code TRAN-CAT-BAL-RECORD (TCATBALF, RECLN 50)}.
 */
@Entity
@Table(name = "tran_cat_balance")
@IdClass(TransactionCategoryBalanceId.class)
public class TransactionCategoryBalance {

    /** {@code TRANCAT-ACCT-ID} {@code 9(11)} */
    @Id
    @Column(name = "trancat_acct_id", nullable = false)
    private Long acctId;

    /** {@code TRANCAT-TYPE-CD} {@code X(02)} */
    @Id
    @Column(name = "trancat_type_cd", nullable = false, length = 2)
    private String typeCd;

    /** {@code TRANCAT-CD} {@code 9(04)} */
    @Id
    @Column(name = "trancat_cd", nullable = false)
    private Integer catCd;

    /** {@code TRAN-CAT-BAL} {@code S9(09)V99} */
    @Column(name = "tran_cat_bal", precision = 11, scale = 2)
    private BigDecimal balance;

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
