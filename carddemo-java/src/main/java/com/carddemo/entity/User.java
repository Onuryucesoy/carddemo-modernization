package com.carddemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Copybook {@code CSUSR01Y}, record {@code SEC-USER-DATA (USRSEC, RECLN 80)}.
 */
@Entity
@Table(name = "users")
public class User {

    /** {@code SEC-USR-ID} {@code X(08)} */
    @Id
    @Column(name = "sec_usr_id", nullable = false, length = 8)
    private String usrId;

    /** {@code SEC-USR-FNAME} {@code X(20)} */
    @Column(name = "sec_usr_fname", length = 20)
    private String firstName;

    /** {@code SEC-USR-LNAME} {@code X(20)} */
    @Column(name = "sec_usr_lname", length = 20)
    private String lastName;

    /** {@code SEC-USR-PWD} {@code X(08), stored as a BCrypt hash} */
    @Column(name = "sec_usr_pwd", nullable = false, length = 100)
    private String pwd;

    /** {@code SEC-USR-TYPE} {@code X(01)} */
    @Column(name = "sec_usr_type", nullable = false, length = 1)
    private Character usrType;

    public String getUsrId() {
        return usrId;
    }

    public void setUsrId(String usrId) {
        this.usrId = usrId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Character getUsrType() {
        return usrType;
    }

    public void setUsrType(Character usrType) {
        this.usrType = usrType;
    }
}
