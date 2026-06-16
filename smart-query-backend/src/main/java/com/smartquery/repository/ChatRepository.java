package com.smartquery.repository;

import com.smartquery.model.ChatMessage;
import com.smartquery.model.ChatSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // === 会话管理 ===

    public List<ChatSession> findSessionsByUserId(Long userId) {
        return jdbcTemplate.query(
            "SELECT * FROM chat_session WHERE user_id = ? AND status = 'ACTIVE' ORDER BY updated_at DESC",
            new ChatSessionRowMapper(),
            userId
        );
    }

    public ChatSession findSessionById(Long id) {
        List<ChatSession> list = jdbcTemplate.query(
            "SELECT * FROM chat_session WHERE id = ?",
            new ChatSessionRowMapper(),
            id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public Long createSession(ChatSession session) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(
                "INSERT INTO chat_session (user_id, title, status) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, session.getUserId());
            ps.setString(2, session.getTitle());
            ps.setString(3, session.getStatus());
            return ps;
        }, keyHolder);
        // 安全获取 ID，处理多列返回的情况
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object id = keyHolder.getKeys().get("ID");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        // 如果上面的方法失败，尝试使用 getKey()
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        throw new RuntimeException("Failed to get generated ID for session");
    }

    public void updateSessionTitle(Long id, String title) {
        jdbcTemplate.update(
            "UPDATE chat_session SET title = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            title, id
        );
    }

    // === 消息管理 ===

    public List<ChatMessage> findMessagesBySessionId(Long sessionId, int limit) {
        return jdbcTemplate.query(
            "SELECT * FROM chat_message WHERE session_id = ? ORDER BY created_at ASC LIMIT ?",
            new ChatMessageRowMapper(),
            sessionId, limit
        );
    }

    public Long createMessage(ChatMessage message) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(
                "INSERT INTO chat_message (session_id, role, content, generated_sql, result_data) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, message.getSessionId());
            ps.setString(2, message.getRole());
            ps.setString(3, message.getContent());
            ps.setString(4, message.getGeneratedSql());
            ps.setString(5, message.getResultData());
            return ps;
        }, keyHolder);
        // 安全获取 ID，处理多列返回的情况
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object id = keyHolder.getKeys().get("ID");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        // 如果上面的方法失败，尝试使用 getKey()
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        throw new RuntimeException("Failed to get generated ID for message");
    }

    // === Row Mappers ===

    static class ChatSessionRowMapper implements RowMapper<ChatSession> {
        @Override
        public ChatSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatSession s = new ChatSession();
            s.setId(rs.getLong("id"));
            s.setUserId(rs.getLong("user_id"));
            s.setTitle(rs.getString("title"));
            s.setStatus(rs.getString("status"));
            return s;
        }
    }

    static class ChatMessageRowMapper implements RowMapper<ChatMessage> {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage m = new ChatMessage();
            m.setId(rs.getLong("id"));
            m.setSessionId(rs.getLong("session_id"));
            m.setRole(rs.getString("role"));
            m.setContent(rs.getString("content"));
            m.setGeneratedSql(rs.getString("generated_sql"));
            m.setResultData(rs.getString("result_data"));
            return m;
        }
    }
}
