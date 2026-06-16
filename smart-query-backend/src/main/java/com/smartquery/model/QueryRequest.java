package com.smartquery.model;

import lombok.Data;

/**
 * 用户查询请求
 */
@Data
public class QueryRequest {
    /**
     * 用户的自然语言问题
     */
    private String question;
    
    /**
     * 会话ID（可选，用于多轮对话）
     */
    private Long sessionId;
}
