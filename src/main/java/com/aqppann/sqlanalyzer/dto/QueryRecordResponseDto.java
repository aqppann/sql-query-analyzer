package com.aqppann.sqlanalyzer.dto;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record QueryRecordResponseDto(
    Long id,
    String sqlText,
    Long executionTimeMs,
    PerformanceStatus status,
    String databaseName,
    String notes,
    LocalDateTime createdAt,
    List<String> recommendations
) {}