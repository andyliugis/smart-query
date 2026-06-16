package com.smartquery.repository;

import com.smartquery.model.SysUser;
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
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SysUser findByUsername(String username) {
        List<SysUser> list = jdbcTemplate.query(
                "SELECT * FROM sys_user WHERE username = ?",
                new SysUserRowMapper(), username);
        return list.isEmpty() ? null : list.get(0);
    }

    public SysUser findById(Long id) {
        List<SysUser> list = jdbcTemplate.query(
                "SELECT * FROM sys_user WHERE id = ?",
                new SysUserRowMapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long save(SysUser user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(
                    "INSERT INTO sys_user (username, password, email, nickname, status) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getNickname());
            ps.setString(5, user.getStatus());
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
        throw new RuntimeException("Failed to get generated ID for user");
    }

    static class SysUserRowMapper implements RowMapper<SysUser> {
        @Override
        public SysUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            SysUser u = new SysUser();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setPassword(rs.getString("password"));
            u.setEmail(rs.getString("email"));
            u.setNickname(rs.getString("nickname"));
            u.setStatus(rs.getString("status"));
            return u;
        }
    }
}
