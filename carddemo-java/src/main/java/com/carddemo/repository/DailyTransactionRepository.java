package com.carddemo.repository;

import com.carddemo.entity.DailyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTransactionRepository extends JpaRepository<DailyTransaction, String> {
}
