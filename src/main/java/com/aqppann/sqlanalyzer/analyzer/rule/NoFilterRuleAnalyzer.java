package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class NoFilterRuleAnalyzer implements SqlRuleAnalyzer {
    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        if (select.getWhere() == null && select.getLimit() == null) {
            recommendations.add("Query has no WHERE or LIMIT clause — may return too many rows");
        }
    }
}
