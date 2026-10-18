package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Copybook {@code CVACT01Y}, record {@code ACCOUNT-RECORD (ACCTDAT, RECLN 300)}.
 */
@Entity
@Table(name = "account")
public class Account {

    /** {@code ACCT-ID} {@code 9(11)} */
    @Id
    @Column(name = "acct_id", nullable = false)
    private Long acctId;

    /** {@code ACCT-ACTIVE-STATUS} {@code X(01)} */
    @Column(name = "acct_active_status", length = 1)
    private Character activeStatus;

    /** {@code ACCT-CURR-BAL} {@code S9(10)V99} */
    @Column(name = "acct_curr_bal", precision = 12, scale = 2)
    private BigDecimal currBal;

    /** {@code ACCT-CREDIT-LIMIT} {@code S9(10)V99} */
    @Column(name = "acct_credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    /** {@code ACCT-CASH-CREDIT-LIMIT} {@code S9(10)V99} */
    @Column(name = "acct_cash_credit_limit", precision = 12, scale = 2)
    private BigDecimal cashCreditLimit;

    /** {@code ACCT-OPEN-DATE} {@code X(10)} */
    @Column(name = "acct_open_date")
    private LocalDate openDate;

    /** {@code ACCT-EXPIRAION-DATE} {@code X(10)} */
    @Column(name = "acct_expiration_date")
    private LocalDate expirationDate;

    /** {@code ACCT-REISSUE-DATE} {@code X(10)} */
    @Column(name = "acct_reissue_date")
    private LocalDate reissueDate;

    /** {@code ACCT-CURR-CYC-CREDIT} {@code S9(10)V99} */
    @Column(name = "acct_curr_cyc_credit", precision = 12, scale = 2)
    private BigDecimal currCycCredit;

    /** {@code ACCT-CURR-CYC-DEBIT} {@code S9(10)V99} */
    @Column(name = "acct_curr_cyc_debit", precision = 12, scale = 2)
    private BigDecimal currCycDebit;

    /** {@code ACCT-ADDR-ZIP} {@code X(10)} */
    @Column(name = "acct_addr_zip", length = 10)
    private String addrZip;

    /** {@code ACCT-GROUP-ID} {@code X(10)} */
    @Column(name = "acct_group_id", length = 10)
    private String groupId;

    public Long getAcctId() {
        return acctId;
    }

    public void setAcctId(Long acctId) {
        this.acctId = acctId;
    }

    public Character getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Character activeStatus) {
        this.activeStatus = activeStatus;
    }

    public BigDecimal getCurrBal() {
        return currBal;
    }

    public void setCurrBal(BigDecimal currBal) {
        this.currBal = currBal;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCashCreditLimit() {
        return cashCreditLimit;
    }

    public void setCashCreditLimit(BigDecimal cashCreditLimit) {
        this.cashCreditLimit = cashCreditLimit;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDate getReissueDate() {
        return reissueDate;
    }

    public void setReissueDate(LocalDate reissueDate) {
        this.reissueDate = reissueDate;
    }

    public BigDecimal getCurrCycCredit() {
        return currCycCredit;
    }

    public void setCurrCycCredit(BigDecimal currCycCredit) {
        this.currCycCredit = currCycCredit;
    }

    public BigDecimal getCurrCycDebit() {
        return currCycDebit;
    }

    public void setCurrCycDebit(BigDecimal currCycDebit) {
        this.currCycDebit = currCycDebit;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
