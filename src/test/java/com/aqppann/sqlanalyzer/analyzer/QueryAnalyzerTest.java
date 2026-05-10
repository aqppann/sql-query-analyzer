package com.aqppann.sqlanalyzer.analyzer;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QueryAnalyzerTest {
    private QueryAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new QueryAnalyzer();
    }

    @Test
    void shouldReturnNormalStatus_whenExecutionTimeIsBelow1000ms() {
        PerformanceStatus status = analyzer.analyzeStatus(500L);
        assertThat(status).isEqualTo(PerformanceStatus.NORMAL);
    }

    @Test
    void shouldReturnSlowStatus_whenExecutionTimeIs1000ms() {
        PerformanceStatus status = analyzer.analyzeStatus(1000L);
        assertThat(status).isEqualTo(PerformanceStatus.SLOW);
    }

    @Test
    void shouldReturnSlowStatus_whenExecutionTimeIsBetween1000And5000ms() {
        PerformanceStatus status = analyzer.analyzeStatus(3000L);
        assertThat(status).isEqualTo(PerformanceStatus.SLOW);
    }

    @Test
    void shouldReturnCriticalStatus_whenExecutionTimeIs5000ms() {
        PerformanceStatus status = analyzer.analyzeStatus(5000L);
        assertThat(status).isEqualTo(PerformanceStatus.CRITICAL);
    }

    @Test
    void shouldReturnCriticalStatus_whenExecutionTimeIsAbove5000ms() {
        PerformanceStatus status = analyzer.analyzeStatus(9999L);
        assertThat(status).isEqualTo(PerformanceStatus.CRITICAL);
    }

    @Test
    void shouldDetectSelectStar() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT * FROM users", 100L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("SELECT *"));
    }

    @Test
    void shouldDetectMissingIndexOnEmail_whenQueryIsSlow() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM users WHERE email = 'test@test.com'", 1500L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("email column"));
    }

    @Test
    void shouldNotDetectMissingIndexOnEmail_whenQueryIsNormal() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM users WHERE email = 'test@test.com'", 500L
        );
        assertThat(recommendations)
                .noneMatch(r -> r.contains("email column"));
    }

    @Test
    void shouldDetectOrderBy_whenQueryIsSlow() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM users ORDER BY created_at", 2000L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("ORDER BY"));
    }

    @Test
    void shouldDetectMissingWhereAndLimit() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM users", 100L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("WHERE or LIMIT"));
    }

    @Test
    void shouldReturnNoIssuesDetected_whenQueryIsClean() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id, name FROM users WHERE id = 1 LIMIT 10", 100L
        );
        assertThat(recommendations)
                .containsExactly("No issues detected");
    }

    @Test
    void shouldDetectNoIn(){
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM orders WHERE id NOT IN (SELECT id FROM archive)", 100L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("NOT IN"));
    }

    @Test
    void shouldDetectOrInWhere() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM users WHERE status = 'active' OR role = 'admin'", 100L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("OR"));
    }

    @Test
    void shouldDetectDistinct() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT DISTINCT id FROM users", 100L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("DISTINCT"));
    }

    @Test
    void shouldDetectSubqueryInWhere() {
        List<String> recommendations = analyzer.generateRecommendations(
                "SELECT id FROM orders WHERE id IN (SELECT id FROM archive)", 100L
        );
        assertThat(recommendations)
                .anyMatch(r -> r.contains("Subquery"));
    }
}