package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class QueryAuditLog {
    private Long id;
    private Long userId;
    private String username;
    private String question;
    private String generatedSql;
    private Long executionTimeMs;
    private Integer resultCount;
    private Boolean success;
    private String errorMessage;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
