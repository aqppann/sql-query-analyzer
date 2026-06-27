package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SelectStarRuleAnalyzer implements SqlRuleAnalyzer {
    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        boolean hasAllColumns = false;
        if (select.getSelectItems() != null) {
            for (SelectItem item : select.getSelectItems()) {
                String itemStr = item.toString().trim();
                if (itemStr.equals("*") || itemStr.endsWith(".*")) {
                    hasAllColumns = true;
                    break;
                }
            }
        }
        if (hasAllColumns) {
            recommendations.add("Avoid SELECT * — specify only required columns to reduce data transfer");
        }
    }
}
