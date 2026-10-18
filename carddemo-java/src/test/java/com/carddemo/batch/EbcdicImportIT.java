package com.carddemo.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.entity.DisclosureGroupId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Same job, loading the EBCDIC (code page 037) copies instead of the ASCII ones. */
@SpringBootTest(properties = {
        "carddemo.import.source=EBCDIC",
        "spring.datasource.url=jdbc:h2:mem:carddemo-ebcdic;DB_CLOSE_DELAY=-1"
})
class EbcdicImportIT {

    @Autowired AccountRepository accounts;
    @Autowired CardRepository cards;
    @Autowired DisclosureGroupRepository discgrp;
    @Autowired TransactionRepository transact;

    @Test
    void loadsEbcdicCopies() {
        assertThat(accounts.count()).isEqualTo(50);
        assertThat(cards.count()).isEqualTo(50);
        assertThat(transact.count()).isEqualTo(300);
        assertThat(accounts.findById(49L).orElseThrow().getAddrZip()).isEqualTo("ZEROAPR");
        assertThat(discgrp.findById(new DisclosureGroupId("DEFAULT", "07", 1)).orElseThrow().getIntRate())
                .isEqualByComparingTo("15.00");
        assertThat(accounts.findById(1L).orElseThrow().getCurrBal()).isEqualByComparingTo("194.00");
    }
}
