package com.smartquery.repository;

import com.smartquery.model.QueryAuditLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class AuditLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(QueryAuditLog log) {
        jdbcTemplate.update(
                "INSERT INTO query_audit_log (user_id, username, question, generated_sql, execution_time_ms, result_count, success, error_message, ip_address, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                log.getUserId(), log.getUsername(), log.getQuestion(), log.getGeneratedSql(),
                log.getExecutionTimeMs(), log.getResultCount(), log.getSuccess(),
                log.getErrorMessage(), log.getIpAddress(), log.getUserAgent());
    }

    public List<QueryAuditLog> findByUserId(Long userId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM query_audit_log WHERE user_id = ? ORDER BY created_at DESC LIMIT ?",
                new QueryAuditLogRowMapper(), userId, limit);
    }

    public List<QueryAuditLog> findAll(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM query_audit_log ORDER BY created_at DESC LIMIT ?",
                new QueryAuditLogRowMapper(), limit);
    }

    static class QueryAuditLogRowMapper implements RowMapper<QueryAuditLog> {
        @Override
        public QueryAuditLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            QueryAuditLog l = new QueryAuditLog();
            l.setId(rs.getLong("id"));
            l.setUserId(rs.getLong("user_id"));
            l.setUsername(rs.getString("username"));
            l.setQuestion(rs.getString("question"));
            l.setGeneratedSql(rs.getString("generated_sql"));
            l.setExecutionTimeMs(rs.getLong("execution_time_ms"));
            l.setResultCount(rs.getInt("result_count"));
            l.setSuccess(rs.getBoolean("success"));
            l.setErrorMessage(rs.getString("error_message"));
            l.setIpAddress(rs.getString("ip_address"));
            l.setUserAgent(rs.getString("user_agent"));
            return l;
        }
    }
}
