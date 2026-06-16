package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DataPermission {
    private Long id;
    private Long userId;
    private String tableName;
    private String columnName;
    private String permissionType;
    private String conditionExpression;
    private LocalDateTime createdAt;
}
