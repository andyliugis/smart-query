package com.smartquery.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 查询反馈模型
 */
@Data
public class QueryFeedback {
    private Long id;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 反馈类型: like / dislike
     */
    private String feedbackType;
    
    /**
     * 反馈内容（可选，用于点踩时收集原因）
     */
    private String content;
    
    /**
     * 原始问题
     */
    private String question;
    
    /**
     * 生成的 SQL
     */
    private String generatedSql;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    public static QueryFeedback like(Long sessionId, Long messageId, Long userId, String question, String sql) {
        QueryFeedback feedback = new QueryFeedback();
        feedback.setSessionId(sessionId);
        feedback.setMessageId(messageId);
        feedback.setUserId(userId);
        feedback.setFeedbackType("like");
        feedback.setQuestion(question);
        feedback.setGeneratedSql(sql);
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }
    
    public static QueryFeedback dislike(Long sessionId, Long messageId, Long userId, String question, String sql, String content) {
        QueryFeedback feedback = new QueryFeedback();
        feedback.setSessionId(sessionId);
        feedback.setMessageId(messageId);
        feedback.setUserId(userId);
        feedback.setFeedbackType("dislike");
        feedback.setQuestion(question);
        feedback.setGeneratedSql(sql);
        feedback.setContent(content);
        feedback.setCreatedAt(LocalDateTime.now());
        return feedback;
    }
}
