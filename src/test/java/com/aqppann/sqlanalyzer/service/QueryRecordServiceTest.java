package com.aqppann.sqlanalyzer.service;

import com.aqppann.sqlanalyzer.analyzer.QueryAnalyzer;
import com.aqppann.sqlanalyzer.dto.QueryRecordRequestDto;
import com.aqppann.sqlanalyzer.dto.QueryRecordResponseDto;
import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import com.aqppann.sqlanalyzer.entity.QueryRecord;
import com.aqppann.sqlanalyzer.repository.QueryRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryRecordServiceTest {
    @Mock
    private QueryRecordRepository repository;

    @Mock
    private QueryAnalyzer analyzer;

    @InjectMocks
    private QueryRecordService service;

    @Test
    void shouldCreateQueryRecord() {
        QueryRecordRequestDto request = new QueryRecordRequestDto();
        request.setSqlText("SELECT * FROM users");
        request.setExecutionTimeMs(1500L);
        request.setDatabaseName("production");

        QueryRecord saved = QueryRecord.builder()
                .id(1L)
                .sqlText("SELECT * FROM users")
                .executionTimeMs(1500L)
                .status(PerformanceStatus.SLOW)
                .databaseName("production")
                .createdAt(LocalDateTime.now())
                .build();

        when(analyzer.analyzeStatus(1500L)).thenReturn(PerformanceStatus.SLOW);
        when(analyzer.generateRecommendations(any(), any())).thenReturn(List.of("Avoid SELECT *"));
        when(repository.save(any())).thenReturn(saved);

        QueryRecordResponseDto response = service.create(request);

        assertThat(response.getStatus()).isEqualTo(PerformanceStatus.SLOW);
        assertThat(response.getRecommendations()).contains("Avoid SELECT *");
        verify(repository, times(1)).save(any());
    }

    @Test
    void shouldThrowException_whenQueryRecordNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldDeleteQueryRecord() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowException_whenDeletingNonExistentRecord() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldReturnTopSlowQueries() {
        QueryRecord record = QueryRecord.builder()
                .id(1L)
                .sqlText("SELECT * FROM orders")
                .executionTimeMs(8000L)
                .status(PerformanceStatus.CRITICAL)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findTop10ByOrderByExecutionTimeMsDesc()).thenReturn(List.of(record));

        List<QueryRecordResponseDto> result = service.findTopSlow();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PerformanceStatus.CRITICAL);
    }
}