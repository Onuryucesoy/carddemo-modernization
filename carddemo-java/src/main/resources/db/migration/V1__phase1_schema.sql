-- Phase 1 schema, generated field-for-field from the copybooks in app/cpy.
-- FILLER fields are dropped. Column names follow the COBOL field names.

-- CVACT01Y: ACCOUNT-RECORD (ACCTDAT, RECLN 300)
CREATE TABLE account (
    acct_id                      BIGINT NOT NULL,
    acct_active_status           CHAR(1),
    acct_curr_bal                DECIMAL(12,2),
    acct_credit_limit            DECIMAL(12,2),
    acct_cash_credit_limit       DECIMAL(12,2),
    acct_open_date               DATE,
    acct_expiration_date         DATE,
    acct_reissue_date            DATE,
    acct_curr_cyc_credit         DECIMAL(12,2),
    acct_curr_cyc_debit          DECIMAL(12,2),
    acct_addr_zip                VARCHAR(10),
    acct_group_id                VARCHAR(10),
    CONSTRAINT pk_account PRIMARY KEY (acct_id)
);

-- CVCUS01Y / CUSTREC: CUSTOMER-RECORD (CUSTDAT, RECLN 500)
CREATE TABLE customer (
    cust_id                      BIGINT NOT NULL,
    cust_first_name              VARCHAR(25),
    cust_middle_name             VARCHAR(25),
    cust_last_name               VARCHAR(25),
    cust_addr_line_1             VARCHAR(50),
    cust_addr_line_2             VARCHAR(50),
    cust_addr_line_3             VARCHAR(50),
    cust_addr_state_cd           VARCHAR(2),
    cust_addr_country_cd         VARCHAR(3),
    cust_addr_zip                VARCHAR(10),
    cust_phone_num_1             VARCHAR(15),
    cust_phone_num_2             VARCHAR(15),
    cust_ssn                     BIGINT,
    cust_govt_issued_id          VARCHAR(20),
    cust_dob                     DATE,
    cust_eft_account_id          VARCHAR(10),
    cust_pri_card_holder_ind     CHAR(1),
    cust_fico_credit_score       INTEGER,
    CONSTRAINT pk_customer PRIMARY KEY (cust_id)
);

-- CVACT02Y: CARD-RECORD (CARDDAT, RECLN 150)
CREATE TABLE card (
    card_num                     VARCHAR(16) NOT NULL,
    card_acct_id                 BIGINT,
    card_cvv_cd                  INTEGER,
    card_embossed_name           VARCHAR(50),
    card_expiration_date         DATE,
    card_active_status           CHAR(1),
    CONSTRAINT pk_card PRIMARY KEY (card_num),
    CONSTRAINT fk_card_account FOREIGN KEY (card_acct_id) REFERENCES account (acct_id)
);
CREATE INDEX ix_card_acct_id ON card (card_acct_id);  -- CARDAIX alternate index

-- CVACT03Y: CARD-XREF-RECORD (CXACAIX, RECLN 50)
CREATE TABLE card_xref (
    xref_card_num                VARCHAR(16) NOT NULL,
    xref_cust_id                 BIGINT,
    xref_acct_id                 BIGINT,
    CONSTRAINT pk_card_xref PRIMARY KEY (xref_card_num),
    CONSTRAINT fk_xref_card FOREIGN KEY (xref_card_num) REFERENCES card (card_num),
    CONSTRAINT fk_xref_customer FOREIGN KEY (xref_cust_id) REFERENCES customer (cust_id),
    CONSTRAINT fk_xref_account FOREIGN KEY (xref_acct_id) REFERENCES account (acct_id)
);
CREATE INDEX ix_xref_acct_id ON card_xref (xref_acct_id);  -- CXACAIX alternate index

-- CVTRA01Y: TRAN-CAT-BAL-RECORD (TCATBALF, RECLN 50)
CREATE TABLE tran_cat_balance (
    trancat_acct_id              BIGINT NOT NULL,
    trancat_type_cd              VARCHAR(2) NOT NULL,
    trancat_cd                   INTEGER NOT NULL,
    tran_cat_bal                 DECIMAL(11,2),
    CONSTRAINT pk_tran_cat_balance PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd)
);

-- CVTRA02Y: DIS-GROUP-RECORD (DISCGRP, RECLN 50)
CREATE TABLE disclosure_group (
    dis_acct_group_id            VARCHAR(10) NOT NULL,
    dis_tran_type_cd             VARCHAR(2) NOT NULL,
    dis_tran_cat_cd              INTEGER NOT NULL,
    dis_int_rate                 DECIMAL(6,2),
    CONSTRAINT pk_disclosure_group PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- CVTRA03Y: TRAN-TYPE-RECORD (TRANTYPE, RECLN 60)
CREATE TABLE transaction_type (
    tran_type                    VARCHAR(2) NOT NULL,
    tran_type_desc               VARCHAR(50),
    CONSTRAINT pk_transaction_type PRIMARY KEY (tran_type)
);

-- CVTRA04Y: TRAN-CAT-RECORD (TRANCATG, RECLN 60)
CREATE TABLE transaction_category (
    tran_type_cd                 VARCHAR(2) NOT NULL,
    tran_cat_cd                  INTEGER NOT NULL,
    tran_cat_type_desc           VARCHAR(50),
    CONSTRAINT pk_transaction_category PRIMARY KEY (tran_type_cd, tran_cat_cd)
);

-- CVTRA05Y: TRAN-RECORD (TRANSACT, RECLN 350)
CREATE TABLE transactions (
    tran_id                      VARCHAR(16) NOT NULL,
    tran_type_cd                 VARCHAR(2),
    tran_cat_cd                  INTEGER,
    tran_source                  VARCHAR(10),
    tran_desc                    VARCHAR(100),
    tran_amt                     DECIMAL(11,2),
    tran_merchant_id             BIGINT,
    tran_merchant_name           VARCHAR(50),
    tran_merchant_city           VARCHAR(50),
    tran_merchant_zip            VARCHAR(10),
    tran_card_num                VARCHAR(16),
    tran_orig_ts                 TIMESTAMP(6),
    tran_proc_ts                 TIMESTAMP(6),
    CONSTRAINT pk_transactions PRIMARY KEY (tran_id)
);

-- CVTRA06Y: DALYTRAN-RECORD (DALYTRAN, RECLN 350)
CREATE TABLE daily_transaction (
    dalytran_id                  VARCHAR(16) NOT NULL,
    dalytran_type_cd             VARCHAR(2),
    dalytran_cat_cd              INTEGER,
    dalytran_source              VARCHAR(10),
    dalytran_desc                VARCHAR(100),
    dalytran_amt                 DECIMAL(11,2),
    dalytran_merchant_id         BIGINT,
    dalytran_merchant_name       VARCHAR(50),
    dalytran_merchant_city       VARCHAR(50),
    dalytran_merchant_zip        VARCHAR(10),
    dalytran_card_num            VARCHAR(16),
    dalytran_orig_ts             TIMESTAMP(6),
    dalytran_proc_ts             TIMESTAMP(6),
    CONSTRAINT pk_daily_transaction PRIMARY KEY (dalytran_id)
);

-- CSUSR01Y: SEC-USER-DATA (USRSEC, RECLN 80)
CREATE TABLE users (
    sec_usr_id                   VARCHAR(8) NOT NULL,
    sec_usr_fname                VARCHAR(20),
    sec_usr_lname                VARCHAR(20),
    sec_usr_pwd                  VARCHAR(100) NOT NULL,
    sec_usr_type                 CHAR(1) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (sec_usr_id),
    CONSTRAINT ck_users_type CHECK (sec_usr_type IN ('A', 'U'))
);

-- CVEXPORT: EXPORT-RECORD header (EXPFILE, RECLN 500)
CREATE TABLE export_record (
    export_sequence_num          BIGINT NOT NULL,
    export_rec_type              CHAR(1),
    export_timestamp             TIMESTAMP(6),
    export_branch_id             VARCHAR(4),
    export_region_code           VARCHAR(5),
    export_record_key            VARCHAR(16),
    CONSTRAINT pk_export_record PRIMARY KEY (export_sequence_num)
);

-- CVTRA07Y TRANSACTION-DETAIL-REPORT (CBTRN03C report line). Not seeded; Phase 3 target.
CREATE TABLE transaction_report_detail (
    tran_report_trans_id   VARCHAR(16) NOT NULL,
    tran_report_account_id VARCHAR(11),
    tran_report_type_cd    VARCHAR(2),
    tran_report_type_desc  VARCHAR(15),
    tran_report_cat_cd     INTEGER,
    tran_report_cat_desc   VARCHAR(29),
    tran_report_source     VARCHAR(10),
    tran_report_amt        DECIMAL(11,2),
    CONSTRAINT pk_transaction_report_detail PRIMARY KEY (tran_report_trans_id)
);
