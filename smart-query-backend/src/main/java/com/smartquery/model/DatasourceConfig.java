package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DatasourceConfig {
    private Long id;
    private String name;
    private String dbType;
    private String jdbcUrl;
    private String username;
    private String password;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
