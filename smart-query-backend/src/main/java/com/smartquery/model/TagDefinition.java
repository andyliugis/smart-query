package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TagDefinition {
    private Long id;
    private String name;
    private String color;
    private String description;
    private LocalDateTime createdAt;
}
