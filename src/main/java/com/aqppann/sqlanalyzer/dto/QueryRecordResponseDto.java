package com.aqppann.sqlanalyzer.dto;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QueryRecordResponseDto {
    private Long id;
    private String sqlText;
    private Long executionTimeMs;
    private PerformanceStatus status;
    private String databaseName;
    private String notes;
    private LocalDateTime createdAt;
    private List<String> recommendations;
}