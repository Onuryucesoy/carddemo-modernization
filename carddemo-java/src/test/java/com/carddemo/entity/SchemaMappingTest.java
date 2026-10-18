package com.carddemo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

/**
 * Runs the Flyway DDL with {@code ddl-auto=validate}, so every entity is checked against the
 * schema, then round-trips the types that the PIC mapping cares about.
 */
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
class SchemaMappingTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void accountKeepsDecimalScaleAndDates() {
        Account a = new Account();
        a.setAcctId(99999999999L);
        a.setActiveStatus('Y');
        a.setCurrBal(new BigDecimal("-9999999999.99"));
        a.setCreditLimit(new BigDecimal("2020.00"));
        a.setOpenDate(LocalDate.of(2014, 11, 20));
        em.persistAndFlush(a);
        em.clear();

        Account loaded = em.find(Account.class, 99999999999L);
        assertThat(loaded.getCurrBal()).isEqualTo(new BigDecimal("-9999999999.99"));
        assertThat(loaded.getCreditLimit().scale()).isEqualTo(2);
        assertThat(loaded.getActiveStatus()).isEqualTo('Y');
        assertThat(loaded.getOpenDate()).isEqualTo(LocalDate.of(2014, 11, 20));
    }

    @Test
    void compositeKeysResolve() {
        TransactionCategoryBalance b = new TransactionCategoryBalance();
        b.setAcctId(1L);
        b.setTypeCd("01");
        b.setCatCd(1);
        b.setBalance(new BigDecimal("12.34"));
        em.persistAndFlush(b);
        DisclosureGroup g = new DisclosureGroup();
        g.setAcctGroupId("A000000000");
        g.setTranTypeCd("01");
        g.setTranCatCd(1);
        g.setIntRate(new BigDecimal("15.00"));
        em.persistAndFlush(g);
        em.clear();

        assertThat(em.find(TransactionCategoryBalance.class, new TransactionCategoryBalanceId(1L, "01", 1))
                .getBalance()).isEqualByComparingTo("12.34");
        assertThat(em.find(DisclosureGroup.class, new DisclosureGroupId("A000000000", "01", 1))
                .getIntRate()).isEqualByComparingTo("15.00");
    }

    @Test
    void cardRequiresExistingAccount() {
        Card c = new Card();
        c.setCardNum("0000000000000001");
        c.setAcctId(12345L);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> em.persistAndFlush(c))
                .hasRootCauseInstanceOf(org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException.class);
    }

    @Test
    void userTypeIsConstrainedToAdminOrUser() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO users (sec_usr_id, sec_usr_pwd, sec_usr_type) VALUES ('X', 'x', 'Z')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
