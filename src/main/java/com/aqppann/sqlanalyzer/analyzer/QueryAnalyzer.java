package com.aqppann.sqlanalyzer.analyzer;

import com.aqppann.sqlanalyzer.entity.PerformanceStatus;
import org.springframework.stereotype.Component;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.schema.Column;

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

        try {
            Statement statement = CCJSqlParserUtil.parse(sqlText);
            if (statement instanceof Select select) {
                if (select.getSelectBody() instanceof PlainSelect plainSelect) {
                    analyzePlainSelect(plainSelect, executionTimeMs, recommendations);
                }
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

    private void analyzePlainSelect(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
        // 1. SELECT * check
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

        // 2. Missing index on email column
        Expression where = select.getWhere();
        if (where != null && referencesColumn(where, "email") && executionTimeMs >= SLOW_THRESHOLD_MS) {
            recommendations.add("Possible missing index on email column — consider adding an index");
        }

        // 3. ORDER BY performance warning
        if (select.getOrderByElements() != null && !select.getOrderByElements().isEmpty() && executionTimeMs >= SLOW_THRESHOLD_MS) {
            recommendations.add("ORDER BY on large datasets can be slow — consider adding an index on sorted column");
        }

        // 4. Wildcards and other expressions in WHERE clause
        if (where != null) {
            analyzeExpression(where, recommendations);
        }

        // 5. No WHERE or LIMIT warning
        if (where == null && select.getLimit() == null) {
            recommendations.add("Query has no WHERE or LIMIT clause — may return too many rows");
        }

        // 6. DISTINCT warning
        if (select.getDistinct() != null) {
            recommendations.add("DISTINCT may indicate a problem with JOIN logic — check for duplicate rows source");
        }

        // 7. JOIN without ON warning
        boolean hasJoinWithoutCondition = false;
        if (select.getJoins() != null) {
            for (Join join : select.getJoins()) {
                boolean hasOn = join.getOnExpression() != null || (join.getOnExpressions() != null && !join.getOnExpressions().isEmpty());
                boolean hasUsing = join.getUsingColumns() != null && !join.getUsingColumns().isEmpty();
                if (!hasOn && !hasUsing && !join.isSimple() && !join.isCross()) {
                    hasJoinWithoutCondition = true;
                    break;
                }
            }
        }
        if (hasJoinWithoutCondition) {
            recommendations.add("JOIN without ON condition may produce a cartesian product — verify join conditions");
        }

        // 8. HAVING without GROUP BY warning
        if (select.getHaving() != null && select.getGroupBy() == null) {
            recommendations.add("HAVING without GROUP BY is unusual — consider using WHERE instead");
        }
    }

    private boolean referencesColumn(Expression expr, String columnName) {
        if (expr == null) return false;
        if (expr instanceof Column col) {
            return col.getColumnName().equalsIgnoreCase(columnName);
        }
        if (expr instanceof Parenthesis parenthesis) {
            return referencesColumn(parenthesis.getExpression(), columnName);
        }
        if (expr instanceof BinaryExpression binary) {
            return referencesColumn(binary.getLeftExpression(), columnName) ||
                   referencesColumn(binary.getRightExpression(), columnName);
        }
        if (expr instanceof InExpression in) {
            return referencesColumn(in.getLeftExpression(), columnName);
        }
        return false;
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