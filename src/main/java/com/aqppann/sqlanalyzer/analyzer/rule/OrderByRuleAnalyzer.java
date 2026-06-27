package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class OrderByRuleAnalyzer implements SqlRuleAnalyzer {
    private static final long SLOW_THRESHOLD_MS = 1000L;

    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        if (select.getOrderByElements() != null && !select.getOrderByElements().isEmpty() && executionTimeMs >= SLOW_THRESHOLD_MS) {
            recommendations.add("ORDER BY on large datasets can be slow — consider adding an index on sorted column");
        }
    }
}
