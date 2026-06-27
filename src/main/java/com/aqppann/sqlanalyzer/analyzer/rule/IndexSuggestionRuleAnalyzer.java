package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class IndexSuggestionRuleAnalyzer implements SqlRuleAnalyzer {
    private static final long SLOW_THRESHOLD_MS = 1000L;

    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        if (executionTimeMs < SLOW_THRESHOLD_MS) {
            return;
        }

        Expression where = select.getWhere();
        if (where != null) {
            Set<String> columns = new HashSet<>();
            findColumnsInExpression(where, columns);
            for (String col : columns) {
                if (col.equalsIgnoreCase("email")) {
                    recommendations.add("Possible missing index on email column — consider adding an index");
                } else {
                    recommendations.add("Possible missing index on column '" + col + "' — consider adding an index");
                }
            }
        }
    }

    private void findColumnsInExpression(Expression expr, Set<String> columns) {
        if (expr == null) return;
        if (expr instanceof Column col) {
            columns.add(col.getColumnName().toLowerCase());
        } else if (expr instanceof Parenthesis parenthesis) {
            findColumnsInExpression(parenthesis.getExpression(), columns);
        } else if (expr instanceof BinaryExpression binary) {
            findColumnsInExpression(binary.getLeftExpression(), columns);
            findColumnsInExpression(binary.getRightExpression(), columns);
        } else if (expr instanceof InExpression in) {
            findColumnsInExpression(in.getLeftExpression(), columns);
        }
    }
}
