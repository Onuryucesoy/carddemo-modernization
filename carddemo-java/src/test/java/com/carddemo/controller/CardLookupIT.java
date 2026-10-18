package com.carddemo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.ExportRecordRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionCategoryRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.repository.TransactionTypeRepository;
import com.carddemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** Full application against H2, seeded at startup from app/data by sampleDataImportJob. */
@SpringBootTest
@AutoConfigureMockMvc
class CardLookupIT {

    static final String ADMIN = "ADMIN001";
    static final String USER = "USER0001";
    static final String PASSWORD = "PASSWORD";

    @Autowired MockMvc mvc;
    @Autowired JobLauncher jobLauncher;
    @Autowired Job sampleDataImportJob;
    @Autowired AccountRepository accounts;
    @Autowired CustomerRepository customers;
    @Autowired CardRepository cards;
    @Autowired CardXrefRepository xrefs;
    @Autowired TransactionCategoryBalanceRepository tcatbal;
    @Autowired DisclosureGroupRepository discgrp;
    @Autowired TransactionTypeRepository trantype;
    @Autowired TransactionCategoryRepository trancatg;
    @Autowired DailyTransactionRepository dalytran;
    @Autowired TransactionRepository transact;
    @Autowired UserRepository users;
    @Autowired ExportRecordRepository exports;

    @Test
    void sampleDataIsLoadedAndImportIsRerunnable() throws Exception {
        assertCounts();
        var exec = jobLauncher.run(sampleDataImportJob, new JobParametersBuilder()
                .addLong("startedAt", System.nanoTime()).toJobParameters());
        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertCounts();
        assertThat(users.findById(ADMIN).orElseThrow().getPwd()).startsWith("$2a$").doesNotContain(PASSWORD);
    }

    private void assertCounts() {
        assertThat(accounts.count()).isEqualTo(50);
        assertThat(customers.count()).isEqualTo(50);
        assertThat(cards.count()).isEqualTo(50);
        assertThat(xrefs.count()).isEqualTo(50);
        assertThat(tcatbal.count()).isEqualTo(50);
        assertThat(discgrp.count()).isEqualTo(51);
        assertThat(trantype.count()).isEqualTo(7);
        assertThat(trancatg.count()).isEqualTo(18);
        assertThat(dalytran.count()).isEqualTo(300);
        assertThat(transact.count()).isEqualTo(300);
        assertThat(users.count()).isEqualTo(10);
        assertThat(exports.count()).isEqualTo(500);
    }

    @Test
    void cardLookupReturnsJoinedCardAccountAndCustomer() throws Exception {
        mvc.perform(get("/api/cards/0500024453765740").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNum").value("0500024453765740"))
                .andExpect(jsonPath("$.acctId").value(50))
                .andExpect(jsonPath("$.acctStatus").value("Y"))
                .andExpect(jsonPath("$.custId").value(50))
                .andExpect(jsonPath("$.custFname").value("Aniya"))
                .andExpect(jsonPath("$.custMname").value("Alba"))
                .andExpect(jsonPath("$.custLname").value("Von"))
                .andExpect(jsonPath("$.card.embossedName").value("Aniya Von"))
                .andExpect(jsonPath("$.card.expirationDate").value("2023-03-09"))
                .andExpect(jsonPath("$.card.expiryMonth").value("03"))
                .andExpect(jsonPath("$.card.expiryYear").value("2023"))
                .andExpect(jsonPath("$.card.activeStatus").value("Y"))
                .andExpect(jsonPath("$.card.cvvCd").doesNotExist())
                .andExpect(jsonPath("$.account.currBal").value(492.00))
                .andExpect(jsonPath("$.account.creditLimit").value(6169.00))
                .andExpect(jsonPath("$.account.cashCreditLimit").value(4587.00))
                .andExpect(jsonPath("$.account.openDate").value("2011-04-22"))
                .andExpect(jsonPath("$.customer.addrLine1").value("1588 Nienow Cape"))
                .andExpect(jsonPath("$.customer.ficoScore").value(623))
                .andExpect(jsonPath("$.customer.ssn").doesNotExist())
                .andExpect(jsonPath("$.infoMessage").value("Displaying requested details"));
    }

    @Test
    void everySampleCardResolves() throws Exception {
        for (var x : xrefs.findAll()) {
            mvc.perform(get("/api/cards/" + x.getXrefCardNum()).with(httpBasic(USER, PASSWORD)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.acctId").value(x.getXrefAcctId()))
                    .andExpect(jsonPath("$.custId").value(x.getXrefCustId()));
        }
    }

    @Test
    void lookupByAccount() throws Exception {
        mvc.perform(get("/api/cards").param("acctId", "00000000050").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardNum").value("0500024453765740"));
        mvc.perform(get("/api/cards").param("acctId", "99999999999").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Did not find this account in cards database"));
        mvc.perform(get("/api/cards").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No input received"));
    }

    @Test
    void errorPathsCarryLegacyMessages() throws Exception {
        mvc.perform(get("/api/cards/1234").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER"))
                .andExpect(jsonPath("$.field").value("cardNum"));
        mvc.perform(get("/api/cards/9999999999999999").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Did not find cards for this search condition"));
        mvc.perform(get("/api/cards/0500024453765740").param("acctId", "00000000001").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Did not find cards for this search condition"));
        mvc.perform(get("/api/cards/0500024453765740").param("acctId", "50").with(httpBasic(ADMIN, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER"));
    }

    @Test
    void signOnIsBackedByUsersTable() throws Exception {
        mvc.perform(get("/api/cards/0500024453765740")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/cards/0500024453765740").with(httpBasic(ADMIN, "WRONGPWD")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/cards/0500024453765740").with(httpBasic("NOBODY01", PASSWORD)))
                .andExpect(status().isUnauthorized());
        // COSGN00C upper-cases user ID and password.
        mvc.perform(get("/api/cards/0500024453765740").with(httpBasic("admin001", "password")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/other").with(httpBasic(ADMIN, PASSWORD))).andExpect(status().isForbidden());
    }
}
