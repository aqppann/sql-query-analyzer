package com.aqppann.sqlanalyzer.analyzer;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QueryAnalyzer {
    private static final long SLOW_THRESHOLD_MS = 1000L;
    private static final long CRITICAL_THRESHOLD_MS = 5000L;

    public PerformanceStatus analyzeStatus(Long executionTimeMs) {
        if (executionTimeMs >= CRITICAL_THRESHOLD_MS) {
            return PerformanceStatus.CRITICAL;
        } else if (executionTimeMs >= SLOW_THRESHOLD_MS) {
            return PerformanceStatus.SLOW;
        }
        return PerformanceStatus.NORMAL;
    }
    public List<String> generateRecommendations(String sqlText, Long executionTimeMs) {
        List<String> recommendations = new ArrayList<>();
        String sql = sqlText.toUpperCase();

        if (sql.contains("SELECT *")) {
            recommendations.add("Avoid SELECT * — specify only required columns to reduce data transfer");
        }

        if (sql.contains("WHERE") && sql.contains("EMAIL") && executionTimeMs >= SLOW_THRESHOLD_MS) {
            recommendations.add("Possible missing index on email column — consider adding an index");
        }

        if (sql.contains("ORDER BY") && executionTimeMs >= SLOW_THRESHOLD_MS) {
            recommendations.add("ORDER BY on large datasets can be slow — consider adding an index on sorted column");
        }

        if (sql.contains("LIKE") && sql.contains("%")) {
            recommendations.add("LIKE with wildcard prevents index usage — consider full-text search");
        }

        if (!sql.contains("WHERE") && !sql.contains("LIMIT")) {
            recommendations.add("Query has no WHERE or LIMIT clause — may return too many rows");
        }

        if (sql.contains("NOT IN")) {
            recommendations.add("NOT IN can be slow on large datasets — consider using NOT EXISTS instead");
        }

        if (sql.contains("WHERE") && sql.contains(" OR ")) {
            recommendations.add("OR in WHERE clause may prevent index usage — consider splitting into UNION queries");
        }

        if (sql.contains("DISTINCT")) {
            recommendations.add("DISTINCT may indicate a problem with JOIN logic — check for duplicate rows source");
        }

        if (sql.contains("JOIN") && !sql.contains("ON") && !sql.contains("USING")) {
            recommendations.add("JOIN without ON condition may produce a cartesian product — verify join conditions");
        }

        if (sql.contains("WHERE") && sql.contains("(SELECT")) {
            recommendations.add("Subquery in WHERE clause can be slow — consider using JOIN instead");
        }

        if (sql.contains("HAVING") && !sql.contains("GROUP BY")) {
            recommendations.add("HAVING without GROUP BY is unusual — consider using WHERE instead");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("No issues detected");
        }
        return recommendations;
    }
}