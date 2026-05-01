package com.aqppann.sqlanalyzer.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class QueryStatisticsDto {
    private long totalCount;
    private long normalCount;
    private long slowCount;
    private long criticalCount;
    private double slowPercent;
    private double criticalPercent;
}
