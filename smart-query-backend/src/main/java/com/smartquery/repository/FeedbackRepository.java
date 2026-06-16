package com.smartquery.repository;

import com.smartquery.model.QueryFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 反馈仓储 - 存储用户对查询结果的反馈
 */
@Repository
public class FeedbackRepository {

    private static final Logger log = LoggerFactory.getLogger(FeedbackRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QueryFeedback> rowMapper = (rs, rowNum) -> {
        QueryFeedback feedback = new QueryFeedback();
        feedback.setId(rs.getLong("id"));
        feedback.setSessionId(rs.getLong("session_id"));
        feedback.setMessageId(rs.getLong("message_id"));
        feedback.setUserId(rs.getLong("user_id"));
        feedback.setFeedbackType(rs.getString("feedback_type"));
        feedback.setContent(rs.getString("content"));
        feedback.setQuestion(rs.getString("question"));
        feedback.setGeneratedSql(rs.getString("generated_sql"));
        if (rs.getTimestamp("created_at") != null) {
            feedback.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return feedback;
    };

    public FeedbackRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存反馈
     */
    public QueryFeedback save(QueryFeedback feedback) {
        String sql = "INSERT INTO query_feedback (session_id, message_id, user_id, feedback_type, content, question, generated_sql, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
                feedback.getSessionId(),
                feedback.getMessageId(),
                feedback.getUserId(),
                feedback.getFeedbackType(),
                feedback.getContent(),
                feedback.getQuestion(),
                feedback.getGeneratedSql(),
                feedback.getCreatedAt());
        
        log.info("保存反馈: type={}, question={}", feedback.getFeedbackType(), feedback.getQuestion());
        return feedback;
    }

    /**
     * 获取用户对特定消息的反馈
     */
    public QueryFeedback findByMessageIdAndUserId(Long messageId, Long userId) {
        String sql = "SELECT * FROM query_feedback WHERE message_id = ? AND user_id = ? LIMIT 1";
        List<QueryFeedback> results = jdbcTemplate.query(sql, rowMapper, messageId, userId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 获取所有点赞的反馈（用于 RAG 优化）
     */
    public List<QueryFeedback> findAllLikes() {
        String sql = "SELECT * FROM query_feedback WHERE feedback_type = 'like' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 获取所有点踩的反馈（用于改进）
     */
    public List<QueryFeedback> findAllDislikes() {
        String sql = "SELECT * FROM query_feedback WHERE feedback_type = 'dislike' ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
