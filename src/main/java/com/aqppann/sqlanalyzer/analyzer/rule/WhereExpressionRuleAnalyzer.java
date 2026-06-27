package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WhereExpressionRuleAnalyzer implements SqlRuleAnalyzer {
    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        Expression where = select.getWhere();
        if (where != null) {
            analyzeExpression(where, recommendations);
        }
    }

    private void analyzeExpression(Expression expr, List<String> recommendations) {
        if (expr == null) return;
        if (expr instanceof LikeExpression like) {
            String value = like.getRightExpression().toString().toUpperCase();
            if (value.contains("'%") || value.contains("'_")) {
                recommendations.add("LIKE with wildcard prevents index usage — consider full-text search");
            }
        } else if (expr instanceof InExpression in) {
            if (in.isNot()) {
                recommendations.add("NOT IN can be slow on large datasets — consider using NOT EXISTS instead");
            }
            analyzeExpression(in.getRightExpression(), recommendations);
        } else if (expr instanceof OrExpression or) {
            recommendations.add("OR in WHERE clause may prevent index usage — consider splitting into UNION queries");
            analyzeExpression(or.getLeftExpression(), recommendations);
            analyzeExpression(or.getRightExpression(), recommendations);
        } else if (expr instanceof ParenthesedSelect) {
            recommendations.add("Subquery in WHERE clause can be slow — consider using JOIN instead");
        } else if (expr instanceof Parenthesis parenthesis) {
            analyzeExpression(parenthesis.getExpression(), recommendations);
        } else if (expr instanceof BinaryExpression binary) {
            analyzeExpression(binary.getLeftExpression(), recommendations);
            analyzeExpression(binary.getRightExpression(), recommendations);
        }
    }
}
