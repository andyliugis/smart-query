package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long sessionId;
    private String role; // "user" 或 "assistant"
    private String content;
    private String generatedSql;
    private String resultData;
    private LocalDateTime createdAt;
}
