package com.carddemo.repository;

import com.carddemo.entity.ExportRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportRecordRepository extends JpaRepository<ExportRecord, Long> {
}
