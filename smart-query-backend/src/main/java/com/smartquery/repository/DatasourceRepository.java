package com.smartquery.repository;

import com.smartquery.model.DatasourceConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DatasourceRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DatasourceConfig> rowMapper = (rs, rowNum) -> {
        DatasourceConfig config = new DatasourceConfig();
        config.setId(rs.getLong("id"));
        config.setName(rs.getString("name"));
        config.setDbType(rs.getString("db_type"));
        config.setJdbcUrl(rs.getString("jdbc_url"));
        config.setUsername(rs.getString("username"));
        config.setPassword(rs.getString("password"));
        config.setStatus(rs.getString("status"));
        config.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        config.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return config;
    };

    public DatasourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DatasourceConfig> findAll() {
        return jdbcTemplate.query("SELECT * FROM datasource_config ORDER BY name", rowMapper);
    }

    public Optional<DatasourceConfig> findById(Long id) {
        List<DatasourceConfig> results = jdbcTemplate.query(
                "SELECT * FROM datasource_config WHERE id = ?", rowMapper, id);
        return results.stream().findFirst();
    }

    public List<DatasourceConfig> findActive() {
        return jdbcTemplate.query(
                "SELECT * FROM datasource_config WHERE status = 'active' ORDER BY name", rowMapper);
    }

    public DatasourceConfig save(DatasourceConfig config) {
        if (config.getId() == null) {
            jdbcTemplate.update(
                "INSERT INTO datasource_config (name, db_type, jdbc_url, username, password, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                config.getName(), config.getDbType(), config.getJdbcUrl(),
                config.getUsername(), config.getPassword(), config.getStatus() != null ? config.getStatus() : "active"
            );
            Long id = jdbcTemplate.queryForObject("SELECT id FROM datasource_config WHERE name = ?", Long.class, config.getName());
            config.setId(id);
        } else {
            jdbcTemplate.update(
                "UPDATE datasource_config SET name = ?, db_type = ?, jdbc_url = ?, username = ?, password = ?, " +
                "status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                config.getName(), config.getDbType(), config.getJdbcUrl(),
                config.getUsername(), config.getPassword(), config.getStatus(), config.getId()
            );
        }
        return config;
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM datasource_config WHERE id = ?", id);
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM datasource_config WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }
}