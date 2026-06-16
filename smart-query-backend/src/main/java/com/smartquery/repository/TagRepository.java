package com.smartquery.repository;

import com.smartquery.model.TagDefinition;
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
public class TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public TagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TagDefinition> findAllTags() {
        return jdbcTemplate.query(
            "SELECT * FROM tag_definition ORDER BY name",
            new TagDefinitionRowMapper()
        );
    }

    public TagDefinition findTagById(Long id) {
        List<TagDefinition> list = jdbcTemplate.query(
            "SELECT * FROM tag_definition WHERE id = ?",
            new TagDefinitionRowMapper(),
            id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    public Long createTag(TagDefinition tag) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(
                "INSERT INTO tag_definition (name, color, description) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getColor() != null ? tag.getColor() : "#3b82f6");
            ps.setString(3, tag.getDescription());
            return ps;
        }, keyHolder);
        
        if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
            Object id = keyHolder.getKeys().get("ID");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        if (keyHolder.getKey() != null) {
            return keyHolder.getKey().longValue();
        }
        throw new RuntimeException("Failed to get generated ID for tag");
    }

    public void updateTag(TagDefinition tag) {
        jdbcTemplate.update(
            "UPDATE tag_definition SET name = ?, color = ?, description = ? WHERE id = ?",
            tag.getName(),
            tag.getColor(),
            tag.getDescription(),
            tag.getId()
        );
    }

    public void deleteTag(Long id) {
        jdbcTemplate.update("DELETE FROM table_tag_relation WHERE tag_id = ?", id);
        jdbcTemplate.update("DELETE FROM tag_definition WHERE id = ?", id);
    }

    public List<TagDefinition> findTagsByTableId(Long tableId) {
        return jdbcTemplate.query(
            "SELECT t.* FROM tag_definition t " +
            "INNER JOIN table_tag_relation r ON t.id = r.tag_id " +
            "WHERE r.table_id = ? ORDER BY t.name",
            new TagDefinitionRowMapper(),
            tableId
        );
    }

    public void addTagToTable(Long tableId, Long tagId) {
        try {
            jdbcTemplate.update(
                "INSERT INTO table_tag_relation (table_id, tag_id) VALUES (?, ?)",
                tableId,
                tagId
            );
        } catch (Exception e) {
            // 忽略唯一键冲突，表示标签已关联
        }
    }

    public void removeTagFromTable(Long tableId, Long tagId) {
        jdbcTemplate.update(
            "DELETE FROM table_tag_relation WHERE table_id = ? AND tag_id = ?",
            tableId,
            tagId
        );
    }

    public void setTagsForTable(Long tableId, List<Long> tagIds) {
        jdbcTemplate.update("DELETE FROM table_tag_relation WHERE table_id = ?", tableId);
        for (Long tagId : tagIds) {
            addTagToTable(tableId, tagId);
        }
    }

    public List<Long> findTableIdsByTagId(Long tagId) {
        return jdbcTemplate.queryForList(
            "SELECT table_id FROM table_tag_relation WHERE tag_id = ?",
            Long.class,
            tagId
        );
    }

    static class TagDefinitionRowMapper implements RowMapper<TagDefinition> {
        @Override
        public TagDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
            TagDefinition tag = new TagDefinition();
            tag.setId(rs.getLong("id"));
            tag.setName(rs.getString("name"));
            tag.setColor(rs.getString("color"));
            tag.setDescription(rs.getString("description"));
            tag.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return tag;
        }
    }
}
