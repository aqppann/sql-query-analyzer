package com.aqppann.sqlanalyzer.analyzer.rule;

import com.aqppann.sqlanalyzer.analyzer.SqlRuleAnalyzer;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class JoinConditionRuleAnalyzer implements SqlRuleAnalyzer {
    @Override
    public void analyze(PlainSelect select, Long executionTimeMs, List<String> recommendations) {
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
    }
}
