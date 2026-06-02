package com.aqppann.sqlanalyzer.dto;

import lombok.Builder;

@Builder
public record QueryStatisticsDto(
    long totalCount,
    long normalCount,
    long slowCount,
    long criticalCount,
    double slowPercent,
    double criticalPercent
) {}
