package com.aqppann.sqlanalyzer.analyzer;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QueryAnalyzer {
    private static final long SLOW_THRESHOLD_MS = 1000L;
    private static final long CRITICAL_THRESHOLD_MS = 5000L;

    private final List<SqlRuleAnalyzer> rules;

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

        try {
            Statement statement = CCJSqlParserUtil.parse(sqlText);
            if (statement instanceof Select select) {
                analyzeSelectBody(select.getSelectBody(), executionTimeMs, recommendations);
            } else {
                recommendations.add("Only SELECT statements are fully analyzed for performance recommendations");
            }
        } catch (Exception e) {
            recommendations.add("Invalid SQL syntax: " + e.getMessage());
        }

        if (recommendations.isEmpty()) {
            recommendations.add("No issues detected");
        }
        return recommendations;
    }

    private void analyzeSelectBody(Object selectBody, Long executionTimeMs, List<String> recommendations) {
        if (selectBody == null) return;
        if (selectBody instanceof PlainSelect plainSelect) {
            for (SqlRuleAnalyzer rule : rules) {
                rule.analyze(plainSelect, executionTimeMs, recommendations);
            }
        } else if (selectBody instanceof SetOperationList setOpList) {
            if (setOpList.getOperations() != null) {
                for (SetOperation op : setOpList.getOperations()) {
                    if (op instanceof UnionOp unionOp && !unionOp.isAll()) {
                        recommendations.add("Found UNION set operator. If duplicates are not a concern or impossible, use UNION ALL for better performance");
                        break;
                    }
                }
            }
            if (setOpList.getSelects() != null) {
                for (Object subBody : setOpList.getSelects()) {
                    analyzeSelectBody(subBody, executionTimeMs, recommendations);
                }
            }
        }
    }
}