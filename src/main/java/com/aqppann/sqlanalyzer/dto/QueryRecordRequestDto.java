package com.aqppann.sqlanalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record QueryRecordRequestDto(
    @NotBlank(message = "SQL text must not be blank")
    String sqlText,

    @NotNull(message = "Execution time must not be null")
    @Min(value = 0, message = "Execution time must be non-negative")
    Long executionTimeMs,

    String databaseName,
    String notes
) {}