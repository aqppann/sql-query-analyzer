package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FunctionOnColumnRuleAnalyzer implements SqlRuleAnalyzer {
    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        Expression where = select.getWhere();
        if (where != null) {
            analyzeExpression(where, recommendations);
        }
    }

    private void analyzeExpression(Expression expr, List<String> recommendations) {
        if (expr == null) return;
        if (expr instanceof Function func) {
            if (func.getParameters() != null) {
                for (Expression param : func.getParameters()) {
                    if (param instanceof Column col) {
                        recommendations.add(String.format(
                                "Using a function (%s) on column '%s' in the WHERE clause prevents standard index usage. Avoid functions on index columns or consider function-based indexes",
                                func.getName().toUpperCase(), col.getColumnName()
                        ));
                    } else {
                        analyzeExpression(param, recommendations);
                    }
                }
            }
        } else if (expr instanceof Parenthesis parenthesis) {
            analyzeExpression(parenthesis.getExpression(), recommendations);
        } else if (expr instanceof BinaryExpression binary) {
            analyzeExpression(binary.getLeftExpression(), recommendations);
            analyzeExpression(binary.getRightExpression(), recommendations);
        }
    }
}
