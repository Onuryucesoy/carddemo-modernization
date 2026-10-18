package com.carddemo.controller;

import com.carddemo.dto.CardDetailResponse;
import com.carddemo.service.CardLookupService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST replacement for the COCRDSLC card detail screen. */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardLookupService service;

    public CardController(CardLookupService service) {
        this.service = service;
    }

    @GetMapping("/{cardNum}")
    public CardDetailResponse getCard(@PathVariable String cardNum,
                                      @RequestParam(required = false) String acctId) {
        return service.findByCardNum(cardNum, acctId);
    }

    @GetMapping
    public List<CardDetailResponse> getCardsByAccount(@RequestParam(required = false) String acctId) {
        return service.findByAcctId(acctId);
    }
}
