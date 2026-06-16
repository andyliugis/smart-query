package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ColumnMetadata {
    private Long id;
    private Long tableId;
    private String columnName;
    private String displayName;
    private String dataType;
    private String role;       // dimension / metric / none
    private String description;
    private String enumValues;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
