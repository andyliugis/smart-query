package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MetricDefinition {
    private Long id;
    private Long tableId;
    private String name;
    private String description;
    private String expression;
    private String aggFunction;  // SUM / AVG / COUNT / MAX / MIN
    private String unit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
