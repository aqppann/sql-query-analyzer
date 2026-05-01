package com.aqppann.sqlanalyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "query_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String sqlText;

    @Column(nullable = false)
    private Long executionTimeMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceStatus status;

    @Column
    private String databaseName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}