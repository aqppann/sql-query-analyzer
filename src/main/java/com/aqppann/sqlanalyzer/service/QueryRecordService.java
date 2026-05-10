package com.aqppann.sqlanalyzer.service;

import com.aqppann.sqlanalyzer.analyzer.QueryAnalyzer;
import com.aqppann.sqlanalyzer.dto.QueryRecordResponseDto;
import com.aqppann.sqlanalyzer.dto.QueryRecordRequestDto;
import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import com.aqppann.sqlanalyzer.entity.QueryRecord;
import com.aqppann.sqlanalyzer.repository.QueryRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aqppann.sqlanalyzer.dto.QueryStatisticsDto;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryRecordService {
    private final QueryRecordRepository repository;
    private final QueryAnalyzer analyzer;

    @Transactional
    public QueryRecordResponseDto create(QueryRecordRequestDto request) {
        PerformanceStatus status = analyzer.analyzeStatus(request.getExecutionTimeMs());
        List<String> recommendations = analyzer.generateRecommendations(
                request.getSqlText(), request.getExecutionTimeMs()
        );

        QueryRecord entity = QueryRecord.builder()
                .sqlText(request.getSqlText())
                .executionTimeMs(request.getExecutionTimeMs())
                .databaseName(request.getDatabaseName())
                .notes(request.getNotes())
                .status(status)
                .build();
        QueryRecord saved = repository.save(entity);
        return toDto(saved, recommendations);
    }
    @Transactional(readOnly = true)
    public Page<QueryRecordResponseDto> findAll(
            PerformanceStatus status, String sqlText, Pageable pageable) {
        if (status != null && sqlText != null) {
            return repository.findByStatusAndSqlTextContainingIgnoreCase(
                    status, sqlText, pageable).map(r -> toDto(r, List.of()));
        }
        if (status != null) {
            return repository.findByStatus(
                    status, pageable).map(r -> toDto(r, List.of()));
        }
        if (sqlText != null) {
            return repository.findBySqlTextContainingIgnoreCase(
                    sqlText, pageable).map(r -> toDto(r, List.of()));
        }
        return repository.findAll(pageable).map(r -> toDto(r, List.of()));
    }
    @Transactional(readOnly = true)
    public QueryRecordResponseDto findById(Long id) {
        QueryRecord record = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Query record not found with id: " + id));
        List<String> recommendations = analyzer.generateRecommendations(
                record.getSqlText(), record.getExecutionTimeMs()
        );
        return toDto(record, recommendations);
    }
    @Transactional
    public QueryRecordResponseDto update(Long id, QueryRecordRequestDto request) {
        QueryRecord record = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Query record not found with id: " + id));

        PerformanceStatus status = analyzer.analyzeStatus(request.getExecutionTimeMs());
        List<String> recommendations = analyzer.generateRecommendations(
                request.getSqlText(), request.getExecutionTimeMs()
        );

        record.setSqlText(request.getSqlText());
        record.setExecutionTimeMs(request.getExecutionTimeMs());
        record.setDatabaseName(request.getDatabaseName());
        record.setNotes(request.getNotes());
        record.setStatus(status);

        QueryRecord saved = repository.save(record);
        return toDto(saved, recommendations);
    }
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Query record not found with id: " + id);
        }
        repository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public List<QueryRecordResponseDto> findTopSlow() {
        return repository.findTop10ByOrderByExecutionTimeMsDesc()
                .stream()
                .map(r -> toDto(r, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public QueryStatisticsDto getStatistics() {
        long total = repository.count();
        long normal = repository.countByStatus(PerformanceStatus.NORMAL);
        long slow = repository.countByStatus(PerformanceStatus.SLOW);
        long critical = repository.countByStatus(PerformanceStatus.CRITICAL);

        return QueryStatisticsDto.builder()
                .totalCount(total)
                .normalCount(normal)
                .slowCount(slow)
                .criticalCount(critical)
                .slowPercent(total > 0 ? (double) slow / total * 100 : 0)
                .criticalPercent(total > 0 ? (double) critical / total * 100 : 0)
                .build();
    }

    private QueryRecordResponseDto toDto(QueryRecord entity, List<String> recommendations) {
        return QueryRecordResponseDto.builder()
                .id(entity.getId())
                .sqlText(entity.getSqlText())
                .executionTimeMs(entity.getExecutionTimeMs())
                .status(entity.getStatus())
                .databaseName(entity.getDatabaseName())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .recommendations(recommendations)
                .build();
    }
}