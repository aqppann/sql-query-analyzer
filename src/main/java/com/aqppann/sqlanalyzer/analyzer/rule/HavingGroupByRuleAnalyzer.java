package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class HavingGroupByRuleAnalyzer implements SqlRuleAnalyzer {
    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        if (select.getHaving() != null && select.getGroupBy() == null) {
            recommendations.add("HAVING without GROUP BY is unusual — consider using WHERE instead");
        }
    }
}
