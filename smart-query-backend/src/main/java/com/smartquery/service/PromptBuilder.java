package com.smartquery.service;

import com.smartquery.model.*;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Prompt 动态组装器 - 从语义层构建 LLM Prompt
 */
@Component
public class PromptBuilder {

    private final MetadataService metadataService;

    public PromptBuilder(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * 根据语义层元数据动态构建 System Prompt
     */
    public String buildSystemPrompt() {
        List<TableMetadata> tables = metadataService.getAllTables();
        List<MetricDefinition> metrics = metadataService.getAllMetrics();
        List<DimensionDefinition> dimensions = metadataService.getAllDimensions();

        StringBuilder sb = new StringBuilder();

        sb.append("你是一位 SQL 专家。请根据用户的自然语言问题，生成可在 H2 数据库上执行的 SQL 查询语句。\n\n");

        // 表结构
        sb.append("## 数据库表结构\n\n");
        for (TableMetadata table : tables) {
            sb.append("表名: ").append(table.getTableName());
            if (table.getDisplayName() != null && !table.getDisplayName().equals(table.getTableName())) {
                sb.append(" (").append(table.getDisplayName()).append(")");
            }
            sb.append("\n");
            if (table.getDescription() != null) {
                sb.append("描述: ").append(table.getDescription()).append("\n");
            }
            sb.append("字段:\n");

            if (table.getColumns() != null) {
                for (ColumnMetadata col : table.getColumns()) {
                    sb.append("  - ").append(col.getColumnName());
                    sb.append(": ").append(col.getDataType());
                    if (col.getDisplayName() != null && !col.getDisplayName().equals(col.getColumnName())) {
                        sb.append(", 业务名称: ").append(col.getDisplayName());
                    }
                    if (col.getDescription() != null) {
                        sb.append(", ").append(col.getDescription());
                    }
                    if (col.getEnumValues() != null) {
                        sb.append(", 可选值: ").append(col.getEnumValues());
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n");
        }

        // 可用指标
        if (!metrics.isEmpty()) {
            sb.append("## 可用指标\n\n");
            for (MetricDefinition m : metrics) {
                sb.append("- ").append(m.getName());
                sb.append(": ").append(m.getAggFunction()).append("(").append(m.getExpression()).append(")");
                if (m.getDescription() != null) {
                    sb.append(" (").append(m.getDescription()).append(")");
                }
                if (m.getUnit() != null) {
                    sb.append("，单位: ").append(m.getUnit());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 可用维度
        if (!dimensions.isEmpty()) {
            sb.append("## 可用维度\n\n");
            for (DimensionDefinition d : dimensions) {
                sb.append("- ").append(d.getName());
                sb.append(": 字段 ").append(d.getColumnName());
                if (d.getDescription() != null) {
                    sb.append(" (").append(d.getDescription()).append(")");
                }
                if (d.getEnumValues() != null) {
                    sb.append("，可选值: ").append(d.getEnumValues());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 规则
        sb.append("## 规则\n\n");
        sb.append("1. 只生成 SELECT 查询语句，不要生成 INSERT/UPDATE/DELETE 等修改语句\n");
        sb.append("2. 只返回纯 SQL 语句，不要添加任何解释或 markdown 标记\n");
        sb.append("3. 计算指标时使用上述指标定义中的表达式和聚合函数\n");
        sb.append("4. 当前日期是 2026年6月15日，\"上个月\"指的是 2026年5月\n");
        sb.append("5. SQL 语句不要以分号结尾\n");

        return sb.toString();
    }
}
