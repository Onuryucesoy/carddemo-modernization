package com.carddemo.repository;

import com.carddemo.entity.Card;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, String> {

    /** CARDAIX: cards by account, in primary-key order. */
    List<Card> findByAcctIdOrderByCardNum(Long acctId);
}
