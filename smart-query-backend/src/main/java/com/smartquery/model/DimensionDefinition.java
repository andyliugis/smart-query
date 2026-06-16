package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DimensionDefinition {
    private Long id;
    private Long tableId;
    private String name;
    private String description;
    private String columnName;
    private String enumValues;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
