package com.aqppann.sqlanalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueryRecordRequestDto {
    @NotBlank(message = "SQL text must not be blank")
    private String sqlText;

    @NotNull(message = "Execution time must not be null")
    @Min(value = 0, message = "Execution time must be non-negative")
    private Long executionTimeMs;

    private String databaseName;
    private String notes;
}