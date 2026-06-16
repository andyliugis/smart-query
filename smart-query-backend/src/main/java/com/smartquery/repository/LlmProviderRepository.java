package com.smartquery.repository;

import com.smartquery.model.LlmProviderConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LLM provider configurations
 */
@Repository
public class LlmProviderRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<LlmProviderConfig> rowMapper = (rs, rowNum) -> {
        LlmProviderConfig config = new LlmProviderConfig();
        config.setId(rs.getLong("id"));
        config.setName(rs.getString("name"));
        config.setDisplayName(rs.getString("display_name"));
        config.setApiKey(rs.getString("api_key"));
        config.setBaseUrl(rs.getString("base_url"));
        config.setModelName(rs.getString("model_name"));
        config.setIsActive(rs.getBoolean("is_active"));
        config.setConfigJson(rs.getString("config_json"));
        config.setCreatedAt(rs.getTimestamp("created_at"));
        config.setUpdatedAt(rs.getTimestamp("updated_at"));
        return config;
    };

    public LlmProviderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<LlmProviderConfig> findAll() {
        return jdbcTemplate.query("SELECT * FROM llm_provider_config ORDER BY name", rowMapper);
    }

    public Optional<LlmProviderConfig> findByName(String name) {
        List<LlmProviderConfig> results = jdbcTemplate.query(
                "SELECT * FROM llm_provider_config WHERE name = ?", rowMapper, name);
        return results.stream().findFirst();
    }

    public List<LlmProviderConfig> findActiveProviders() {
        return jdbcTemplate.query(
                "SELECT * FROM llm_provider_config WHERE is_active = TRUE ORDER BY name", rowMapper);
    }

    public LlmProviderConfig save(LlmProviderConfig config) {
        if (config.getId() == null) {
            // Insert
            jdbcTemplate.update(
                "INSERT INTO llm_provider_config (name, display_name, api_key, base_url, model_name, is_active, config_json, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                config.getName(), config.getDisplayName(), config.getApiKey(), 
                config.getBaseUrl(), config.getModelName(), config.getIsActive(), config.getConfigJson()
            );
            // Get generated ID
            Long id = jdbcTemplate.queryForObject("SELECT id FROM llm_provider_config WHERE name = ?", Long.class, config.getName());
            config.setId(id);
        } else {
            // Update
            jdbcTemplate.update(
                "UPDATE llm_provider_config SET display_name = ?, api_key = ?, base_url = ?, model_name = ?, " +
                "is_active = ?, config_json = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                config.getDisplayName(), config.getApiKey(), config.getBaseUrl(),
                config.getModelName(), config.getIsActive(), config.getConfigJson(), config.getId()
            );
        }
        return config;
    }

    public void deleteByName(String name) {
        jdbcTemplate.update("DELETE FROM llm_provider_config WHERE name = ?", name);
    }

    public boolean existsByName(String name) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM llm_provider_config WHERE name = ?", Integer.class, name);
        return count != null && count > 0;
    }
}