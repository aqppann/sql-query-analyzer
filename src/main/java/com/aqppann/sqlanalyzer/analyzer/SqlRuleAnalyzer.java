package com.aqppann.sqlanalyzer.analyzer;

import net.sf.jsqlparser.statement.select.PlainSelect;
import java.util.List;

public interface SqlRuleAnalyzer {
    void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations);
}
