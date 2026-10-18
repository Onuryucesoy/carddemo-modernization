package com.carddemo.service;

import static com.carddemo.service.CardLookupMessages.ACCT_HAS_NO_CARDS;
import static com.carddemo.service.CardLookupMessages.ACCT_NOT_IN_MASTER;
import static com.carddemo.service.CardLookupMessages.ACCT_NOT_NUMERIC;
import static com.carddemo.service.CardLookupMessages.ACCT_NOT_PROVIDED;
import static com.carddemo.service.CardLookupMessages.CARD_NOT_FOUND;
import static com.carddemo.service.CardLookupMessages.CARD_NOT_IN_XREF;
import static com.carddemo.service.CardLookupMessages.CARD_NOT_NUMERIC;
import static com.carddemo.service.CardLookupMessages.CARD_NOT_PROVIDED;
import static com.carddemo.service.CardLookupMessages.CUST_NOT_IN_MASTER;
import static com.carddemo.service.CardLookupMessages.FOUND;
import static com.carddemo.service.CardLookupMessages.NO_INPUT;

import com.carddemo.dto.CardDetailResponse;
import com.carddemo.dto.CardDetailResponse.AccountInfo;
import com.carddemo.dto.CardDetailResponse.CardInfo;
import com.carddemo.dto.CardDetailResponse.CustomerInfo;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Card detail lookup, migrated from COCRDSLC (CICS transaction CCDL, map COCRDSL).
 *
 * <p>Input edits follow 2200-EDIT-MAP-INPUTS: account is edited before card, the first error
 * message wins, and "No input received" overrides when neither is supplied. The card is read
 * by primary key (9100-GETCARD-BYACCTCARD) or by account through CARDAIX (9150-GETCARD-BYACCT).
 * Account and customer are then resolved through CARDXREF the way COACTVWC does it.
 */
@Service
@Transactional(readOnly = true)
public class CardLookupService {

    private final CardRepository cards;
    private final CardXrefRepository xrefs;
    private final AccountRepository accounts;
    private final CustomerRepository customers;

    public CardLookupService(CardRepository cards, CardXrefRepository xrefs, AccountRepository accounts,
                             CustomerRepository customers) {
        this.cards = cards;
        this.xrefs = xrefs;
        this.accounts = accounts;
        this.customers = customers;
    }

    /**
     * Lookup by card number, optionally restricted to an account.
     *
     * @param cardNumInput the card number as typed ({@code CARDSIDI})
     * @param acctIdInput  the account number as typed ({@code ACCTSIDI}), or {@code null} if not supplied
     */
    public CardDetailResponse findByCardNum(String cardNumInput, String acctIdInput) {
        Optional<Long> acctId = Optional.empty();
        boolean acctBlank = acctIdInput == null || isBlank(acctIdInput);
        if (acctIdInput != null) {
            acctId = editAccount(acctIdInput, false);
        }
        if (isBlank(cardNumInput)) {
            throw badRequest(acctBlank ? NO_INPUT : CARD_NOT_PROVIDED, acctBlank ? null : "cardNum");
        }
        String cardNum = editCard(cardNumInput);

        Card card = cards.findById(cardNum)
                .orElseThrow(() -> notFound(CARD_NOT_FOUND, "cardNum"));
        if (acctId.isPresent() && !acctId.get().equals(card.getAcctId())) {
            throw notFound(CARD_NOT_FOUND, "cardNum");
        }
        return resolve(card);
    }

    /** Lookup by account through the CARDAIX alternate index. */
    public List<CardDetailResponse> findByAcctId(String acctIdInput) {
        if (acctIdInput == null || isBlank(acctIdInput)) {
            throw badRequest(NO_INPUT, null);
        }
        Long acctId = editAccount(acctIdInput, true).orElseThrow();
        List<Card> found = cards.findByAcctIdOrderByCardNum(acctId);
        if (found.isEmpty()) {
            throw notFound(ACCT_HAS_NO_CARDS, "acctId");
        }
        return found.stream().map(this::resolve).toList();
    }

    /**
     * 2210-EDIT-ACCOUNT. Blank, {@code *} and all zeros mean "not supplied"; anything else must be
     * exactly 11 digits.
     */
    private Optional<Long> editAccount(String input, boolean required) {
        if (isBlank(input)) {
            if (required) {
                throw badRequest(ACCT_NOT_PROVIDED, "acctId");
            }
            return Optional.empty();
        }
        if (!input.matches("\\d{11}")) {
            throw badRequest(ACCT_NOT_NUMERIC, "acctId");
        }
        return Optional.of(Long.parseLong(input));
    }

    /** 2220-EDIT-CARD: anything other than exactly 16 digits is rejected. */
    private String editCard(String input) {
        if (!input.matches("\\d{16}")) {
            throw badRequest(CARD_NOT_NUMERIC, "cardNum");
        }
        return input;
    }

    private CardDetailResponse resolve(Card card) {
        CardXref xref = xrefs.findById(card.getCardNum())
                .orElseThrow(() -> notFound(CARD_NOT_IN_XREF, "cardNum"));
        Account account = accounts.findById(xref.getXrefAcctId())
                .orElseThrow(() -> notFound(ACCT_NOT_IN_MASTER, "acctId"));
        Customer customer = customers.findById(xref.getXrefCustId())
                .orElseThrow(() -> notFound(CUST_NOT_IN_MASTER, null));
        return toResponse(card, account, customer);
    }

    static CardDetailResponse toResponse(Card card, Account a, Customer c) {
        String month = null;
        String year = null;
        if (card.getExpirationDate() != null) {
            month = String.format("%02d", card.getExpirationDate().getMonthValue());
            year = String.valueOf(card.getExpirationDate().getYear());
        }
        return new CardDetailResponse(
                a.getAcctId(), a.getActiveStatus(), card.getCardNum(),
                c.getCustId(), c.getFirstName(), c.getMiddleName(), c.getLastName(),
                new CardInfo(card.getEmbossedName(), card.getExpirationDate(), month, year, card.getActiveStatus()),
                new AccountInfo(a.getActiveStatus(), a.getCurrBal(), a.getCreditLimit(), a.getCashCreditLimit(),
                        a.getOpenDate(), a.getExpirationDate(), a.getReissueDate(), a.getCurrCycCredit(),
                        a.getCurrCycDebit(), a.getAddrZip(), a.getGroupId()),
                new CustomerInfo(c.getFirstName(), c.getMiddleName(), c.getLastName(), c.getAddrLine1(),
                        c.getAddrLine2(), c.getAddrLine3(), c.getAddrStateCd(), c.getAddrCountryCd(), c.getAddrZip(),
                        c.getPhoneNum1(), c.getPhoneNum2(), c.getDob(), c.getPriCardHolderInd(), c.getFicoScore()),
                FOUND);
    }

    /** Blank, {@code *} (2200-EDIT-MAP-INPUTS) or numeric zero ({@code CC-*-N EQUAL ZEROS}). */
    private static boolean isBlank(String input) {
        String s = input.strip();
        return s.isEmpty() || s.equals("*") || s.matches("0+");
    }

    private static CardLookupException badRequest(String message, String field) {
        return new CardLookupException(HttpStatus.BAD_REQUEST, message, field);
    }

    private static CardLookupException notFound(String message, String field) {
        return new CardLookupException(HttpStatus.NOT_FOUND, message, field);
    }
}
