package com.aqppann.sqlanalyzer.repository;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import com.aqppann.sqlanalyzer.entity.QueryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryRecordRepository extends JpaRepository<QueryRecord, Long>, JpaSpecificationExecutor<QueryRecord> {
    List<QueryRecord> findTop10ByOrderByExecutionTimeMsDesc();
    long countByStatus(PerformanceStatus status);
}
