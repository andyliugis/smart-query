package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TableMetadata {
    private Long id;
    private Long datasourceId;
    private String tableName;
    private String displayName;
    private String description;
    private LocalDateTime syncTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 关联的字段列表 (非持久化，查询时填充) */
    private List<ColumnMetadata> columns;
    
    /** 关联的标签列表 (非持久化，查询时填充) */
    private List<TagDefinition> tags;
}
