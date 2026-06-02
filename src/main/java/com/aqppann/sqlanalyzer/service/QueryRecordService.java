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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aqppann.sqlanalyzer.dto.QueryStatisticsDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryRecordService {
    private final QueryRecordRepository repository;
    private final QueryAnalyzer analyzer;

    @Transactional
    public QueryRecordResponseDto create(QueryRecordRequestDto request) {
        PerformanceStatus status = analyzer.analyzeStatus(request.executionTimeMs());
        List<String> recommendations = analyzer.generateRecommendations(
                request.sqlText(), request.executionTimeMs()
        );

        QueryRecord entity = QueryRecord.builder()
                .sqlText(request.sqlText())
                .executionTimeMs(request.executionTimeMs())
                .databaseName(request.databaseName())
                .notes(request.notes())
                .status(status)
                .build();
        QueryRecord saved = repository.save(entity);
        return toDto(saved, recommendations);
    }

    @Transactional(readOnly = true)
    public Page<QueryRecordResponseDto> findAll(
            PerformanceStatus status,
            String sqlText,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {

        Specification<QueryRecord> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (sqlText != null && !sqlText.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("sqlText")), "%" + sqlText.toLowerCase() + "%"));
        }
        if (from != null && to != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), from, to));
        }

        return repository.findAll(spec, pageable).map(r -> toDto(r, List.of()));
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

        PerformanceStatus status = analyzer.analyzeStatus(request.executionTimeMs());
        List<String> recommendations = analyzer.generateRecommendations(
                request.sqlText(), request.executionTimeMs()
        );

        record.setSqlText(request.sqlText());
        record.setExecutionTimeMs(request.executionTimeMs());
        record.setDatabaseName(request.databaseName());
        record.setNotes(request.notes());
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