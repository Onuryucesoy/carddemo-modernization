package com.carddemo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carddemo.dto.CardDetailResponse;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardLookupServiceTest {

    static final String CARD = "0500024453765740";
    static final String ACCT = "00000000050";

    @Mock CardRepository cards;
    @Mock CardXrefRepository xrefs;
    @Mock AccountRepository accounts;
    @Mock CustomerRepository customers;

    CardLookupService service;

    @BeforeEach
    void setUp() {
        service = new CardLookupService(cards, xrefs, accounts, customers);
        Card card = new Card();
        card.setCardNum(CARD);
        card.setAcctId(50L);
        card.setEmbossedName("Aniya Von");
        card.setExpirationDate(LocalDate.of(2023, 3, 9));
        card.setActiveStatus('Y');
        CardXref x = new CardXref();
        x.setXrefCardNum(CARD);
        x.setXrefAcctId(50L);
        x.setXrefCustId(7L);
        Account a = new Account();
        a.setAcctId(50L);
        a.setActiveStatus('Y');
        Customer c = new Customer();
        c.setCustId(7L);
        c.setFirstName("Aniya");
        c.setLastName("Von");
        when(cards.findById(CARD)).thenReturn(Optional.of(card));
        when(cards.findByAcctIdOrderByCardNum(50L)).thenReturn(List.of(card));
        when(xrefs.findById(CARD)).thenReturn(Optional.of(x));
        when(accounts.findById(50L)).thenReturn(Optional.of(a));
        when(customers.findById(7L)).thenReturn(Optional.of(c));
    }

    @Test
    void resolvesCardAccountAndCustomerThroughXref() {
        CardDetailResponse r = service.findByCardNum(CARD, null);
        assertThat(r.cardNum()).isEqualTo(CARD);
        assertThat(r.acctId()).isEqualTo(50L);
        assertThat(r.acctStatus()).isEqualTo('Y');
        assertThat(r.custId()).isEqualTo(7L);
        assertThat(r.custFname()).isEqualTo("Aniya");
        assertThat(r.card().expiryMonth()).isEqualTo("03");
        assertThat(r.card().expiryYear()).isEqualTo("2023");
        assertThat(r.infoMessage()).isEqualTo(CardLookupMessages.FOUND);
    }

    @Test
    void matchingAccountFilterIsAccepted() {
        assertThat(service.findByCardNum(CARD, ACCT).acctId()).isEqualTo(50L);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', nullValues = "NULL", value = {
        // cardNum          | acctId        | status | message
        "*                  | NULL          | 400 | No input received",
        "0000000000000000   | *             | 400 | No input received",
        "*                  | 00000000050   | 400 | Card number not provided",
        "411111111111111    | NULL          | 400 | CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER",
        "41111111111111112  | NULL          | 400 | CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER",
        "4111abcd11111111   | NULL          | 400 | CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER",
        "4111abcd11111111   | 5             | 400 | ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER",
        "9999999999999999   | NULL          | 404 | Did not find cards for this search condition",
        "0500024453765740   | 00000000049   | 404 | Did not find cards for this search condition",
    })
    void cardPathErrors(String cardNum, String acctId, int status, String message) {
        assertThatThrownBy(() -> service.findByCardNum(cardNum, acctId))
                .isInstanceOfSatisfying(CardLookupException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(status);
                    assertThat(e.getMessage()).isEqualTo(message);
                });
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', nullValues = "NULL", value = {
        "NULL          | 400 | No input received",
        "00000000000   | 400 | No input received",
        "*             | 400 | No input received",
        "5             | 400 | ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER",
        "0000000005X   | 400 | ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER",
        "99999999999   | 404 | Did not find this account in cards database",
    })
    void accountPathErrors(String acctId, int status, String message) {
        assertThatThrownBy(() -> service.findByAcctId(acctId))
                .isInstanceOfSatisfying(CardLookupException.class, e -> {
                    assertThat(e.getStatus().value()).isEqualTo(status);
                    assertThat(e.getMessage()).isEqualTo(message);
                });
    }

    @Test
    void validationHappensBeforeAnyRead() {
        assertThatThrownBy(() -> service.findByCardNum("abc", null)).isInstanceOf(CardLookupException.class);
        verifyNoInteractions(cards, xrefs, accounts, customers);
    }

    @Test
    void lookupByAccountUsesAlternateIndex() {
        assertThat(service.findByAcctId(ACCT)).singleElement()
                .satisfies(r -> assertThat(r.cardNum()).isEqualTo(CARD));
    }

    @Test
    void missingXrefAccountOrCustomerAreDistinctErrors() {
        when(customers.findById(7L)).thenReturn(Optional.empty());
        assertError(HttpStatus.NOT_FOUND, "Did not find associated customer in master file");
        when(accounts.findById(50L)).thenReturn(Optional.empty());
        assertError(HttpStatus.NOT_FOUND, "Did not find this account in account master file");
        when(xrefs.findById(CARD)).thenReturn(Optional.empty());
        assertError(HttpStatus.NOT_FOUND, "Did not find this account in account card xref file");
    }

    private void assertError(HttpStatus status, String message) {
        assertThatThrownBy(() -> service.findByCardNum(CARD, null))
                .isInstanceOfSatisfying(CardLookupException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(status);
                    assertThat(e.getMessage()).isEqualTo(message);
                });
    }
}
