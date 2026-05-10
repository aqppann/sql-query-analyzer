package com.aqppann.sqlanalyzer.repository;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import com.aqppann.sqlanalyzer.entity.QueryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QueryRecordRepository extends JpaRepository<QueryRecord, Long> {
    Page<QueryRecord> findByStatus(PerformanceStatus status, Pageable pageable);
    List<QueryRecord> findTop10ByOrderByExecutionTimeMsDesc();
    long countByStatus(PerformanceStatus status);

    Page<QueryRecord> findByStatusAndSqlTextContainingIgnoreCase(
            PerformanceStatus status, String sqlText, Pageable pageable);

    Page<QueryRecord> findBySqlTextContainingIgnoreCase(
            String sqlText, Pageable pageable);

    Page<QueryRecord> findByCreatedAtBetween(
            LocalDateTime from, LocalDateTime to, Pageable pageable
    );

    Page<QueryRecord> findByStatusAndCreatedAtBetween(
            PerformanceStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable
    );

    Page<QueryRecord> findBySqlTextContainingIgnoreCaseAndCreatedAtBetween(
            String sqlText, LocalDateTime from, LocalDateTime to, Pageable pageable
    );

    Page<QueryRecord> findByStatusAndSqlTextContainingIgnoreCaseAndCreatedAtBetween(
            PerformanceStatus status, String sqlText, LocalDateTime from, LocalDateTime to, Pageable pageable
    );
}
