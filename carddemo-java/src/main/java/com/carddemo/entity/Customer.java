package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Copybook {@code CVCUS01Y / CUSTREC}, record {@code CUSTOMER-RECORD (CUSTDAT, RECLN 500)}.
 */
@Entity
@Table(name = "customer")
public class Customer {

    /** {@code CUST-ID} {@code 9(09)} */
    @Id
    @Column(name = "cust_id", nullable = false)
    private Long custId;

    /** {@code CUST-FIRST-NAME} {@code X(25)} */
    @Column(name = "cust_first_name", length = 25)
    private String firstName;

    /** {@code CUST-MIDDLE-NAME} {@code X(25)} */
    @Column(name = "cust_middle_name", length = 25)
    private String middleName;

    /** {@code CUST-LAST-NAME} {@code X(25)} */
    @Column(name = "cust_last_name", length = 25)
    private String lastName;

    /** {@code CUST-ADDR-LINE-1} {@code X(50)} */
    @Column(name = "cust_addr_line_1", length = 50)
    private String addrLine1;

    /** {@code CUST-ADDR-LINE-2} {@code X(50)} */
    @Column(name = "cust_addr_line_2", length = 50)
    private String addrLine2;

    /** {@code CUST-ADDR-LINE-3} {@code X(50)} */
    @Column(name = "cust_addr_line_3", length = 50)
    private String addrLine3;

    /** {@code CUST-ADDR-STATE-CD} {@code X(02)} */
    @Column(name = "cust_addr_state_cd", length = 2)
    private String addrStateCd;

    /** {@code CUST-ADDR-COUNTRY-CD} {@code X(03)} */
    @Column(name = "cust_addr_country_cd", length = 3)
    private String addrCountryCd;

    /** {@code CUST-ADDR-ZIP} {@code X(10)} */
    @Column(name = "cust_addr_zip", length = 10)
    private String addrZip;

    /** {@code CUST-PHONE-NUM-1} {@code X(15)} */
    @Column(name = "cust_phone_num_1", length = 15)
    private String phoneNum1;

    /** {@code CUST-PHONE-NUM-2} {@code X(15)} */
    @Column(name = "cust_phone_num_2", length = 15)
    private String phoneNum2;

    /** {@code CUST-SSN} {@code 9(09)} */
    @Column(name = "cust_ssn")
    private Long ssn;

    /** {@code CUST-GOVT-ISSUED-ID} {@code X(20)} */
    @Column(name = "cust_govt_issued_id", length = 20)
    private String govtIssuedId;

    /** {@code CUST-DOB-YYYY-MM-DD} {@code X(10)} */
    @Column(name = "cust_dob")
    private LocalDate dob;

    /** {@code CUST-EFT-ACCOUNT-ID} {@code X(10)} */
    @Column(name = "cust_eft_account_id", length = 10)
    private String eftAccountId;

    /** {@code CUST-PRI-CARD-HOLDER-IND} {@code X(01)} */
    @Column(name = "cust_pri_card_holder_ind", length = 1)
    private Character priCardHolderInd;

    /** {@code CUST-FICO-CREDIT-SCORE} {@code 9(03)} */
    @Column(name = "cust_fico_credit_score")
    private Integer ficoScore;

    public Long getCustId() {
        return custId;
    }

    public void setCustId(Long custId) {
        this.custId = custId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddrLine1() {
        return addrLine1;
    }

    public void setAddrLine1(String addrLine1) {
        this.addrLine1 = addrLine1;
    }

    public String getAddrLine2() {
        return addrLine2;
    }

    public void setAddrLine2(String addrLine2) {
        this.addrLine2 = addrLine2;
    }

    public String getAddrLine3() {
        return addrLine3;
    }

    public void setAddrLine3(String addrLine3) {
        this.addrLine3 = addrLine3;
    }

    public String getAddrStateCd() {
        return addrStateCd;
    }

    public void setAddrStateCd(String addrStateCd) {
        this.addrStateCd = addrStateCd;
    }

    public String getAddrCountryCd() {
        return addrCountryCd;
    }

    public void setAddrCountryCd(String addrCountryCd) {
        this.addrCountryCd = addrCountryCd;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getPhoneNum1() {
        return phoneNum1;
    }

    public void setPhoneNum1(String phoneNum1) {
        this.phoneNum1 = phoneNum1;
    }

    public String getPhoneNum2() {
        return phoneNum2;
    }

    public void setPhoneNum2(String phoneNum2) {
        this.phoneNum2 = phoneNum2;
    }

    public Long getSsn() {
        return ssn;
    }

    public void setSsn(Long ssn) {
        this.ssn = ssn;
    }

    public String getGovtIssuedId() {
        return govtIssuedId;
    }

    public void setGovtIssuedId(String govtIssuedId) {
        this.govtIssuedId = govtIssuedId;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getEftAccountId() {
        return eftAccountId;
    }

    public void setEftAccountId(String eftAccountId) {
        this.eftAccountId = eftAccountId;
    }

    public Character getPriCardHolderInd() {
        return priCardHolderInd;
    }

    public void setPriCardHolderInd(Character priCardHolderInd) {
        this.priCardHolderInd = priCardHolderInd;
    }

    public Integer getFicoScore() {
        return ficoScore;
    }

    public void setFicoScore(Integer ficoScore) {
        this.ficoScore = ficoScore;
    }
}
