package com.smartquery.repository;

import com.smartquery.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 元数据 CRUD 仓库
 */
@Repository
public class MetadataRepository {

    private final JdbcTemplate jdbcTemplate;

    public MetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ===== 表元数据 =====

    public List<TableMetadata> findAllTables() {
        return jdbcTemplate.query(
                "SELECT * FROM table_metadata ORDER BY id",
                new TableMetadataRowMapper());
    }

    public TableMetadata findTableById(Long id) {
        List<TableMetadata> list = jdbcTemplate.query(
                "SELECT * FROM table_metadata WHERE id = ?",
                new TableMetadataRowMapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public TableMetadata findTableByName(String tableName) {
        List<TableMetadata> list = jdbcTemplate.query(
                "SELECT * FROM table_metadata WHERE table_name = ?",
                new TableMetadataRowMapper(), tableName);
        return list.isEmpty() ? null : list.get(0);
    }

    public void insertTable(TableMetadata table) {
        jdbcTemplate.update(
                "INSERT INTO table_metadata (datasource_id, table_name, display_name, description, sync_time) VALUES (?, ?, ?, ?, ?)",
                table.getDatasourceId(), table.getTableName(), table.getDisplayName(),
                table.getDescription(), table.getSyncTime());
    }

    public void updateTable(TableMetadata table) {
        jdbcTemplate.update(
                "UPDATE table_metadata SET display_name=?, description=?, updated_at=CURRENT_TIMESTAMP WHERE id=?",
                table.getDisplayName(), table.getDescription(), table.getId());
    }

    // ===== 字段元数据 =====

    public List<ColumnMetadata> findColumnsByTableId(Long tableId) {
        return jdbcTemplate.query(
                "SELECT * FROM column_metadata WHERE table_id = ? ORDER BY id",
                new ColumnMetadataRowMapper(), tableId);
    }

    public void insertColumn(ColumnMetadata col) {
        jdbcTemplate.update(
                "INSERT INTO column_metadata (table_id, column_name, display_name, data_type, role, description, enum_values) VALUES (?, ?, ?, ?, ?, ?, ?)",
                col.getTableId(), col.getColumnName(), col.getDisplayName(),
                col.getDataType(), col.getRole(), col.getDescription(), col.getEnumValues());
    }

    public void updateColumn(ColumnMetadata col) {
        jdbcTemplate.update(
                "UPDATE column_metadata SET display_name=?, role=?, description=?, enum_values=?, updated_at=CURRENT_TIMESTAMP WHERE id=?",
                col.getDisplayName(), col.getRole(), col.getDescription(),
                col.getEnumValues(), col.getId());
    }

    public void deleteColumnsByTableId(Long tableId) {
        jdbcTemplate.update("DELETE FROM column_metadata WHERE table_id = ?", tableId);
    }

    // ===== 指标定义 =====

    public List<MetricDefinition> findAllMetrics() {
        return jdbcTemplate.query("SELECT * FROM metric_definition ORDER BY id", new MetricRowMapper());
    }

    public List<MetricDefinition> findMetricsByTableId(Long tableId) {
        return jdbcTemplate.query(
                "SELECT * FROM metric_definition WHERE table_id = ? ORDER BY id",
                new MetricRowMapper(), tableId);
    }

    public void insertMetric(MetricDefinition metric) {
        jdbcTemplate.update(
                "INSERT INTO metric_definition (table_id, name, description, expression, agg_function, unit) VALUES (?, ?, ?, ?, ?, ?)",
                metric.getTableId(), metric.getName(), metric.getDescription(),
                metric.getExpression(), metric.getAggFunction(), metric.getUnit());
    }

    public void deleteMetric(Long id) {
        jdbcTemplate.update("DELETE FROM metric_definition WHERE id = ?", id);
    }

    // ===== 维度定义 =====

    public List<DimensionDefinition> findAllDimensions() {
        return jdbcTemplate.query("SELECT * FROM dimension_definition ORDER BY id", new DimensionRowMapper());
    }

    public List<DimensionDefinition> findDimensionsByTableId(Long tableId) {
        return jdbcTemplate.query(
                "SELECT * FROM dimension_definition WHERE table_id = ? ORDER BY id",
                new DimensionRowMapper(), tableId);
    }

    public void insertDimension(DimensionDefinition dim) {
        jdbcTemplate.update(
                "INSERT INTO dimension_definition (table_id, name, description, column_name, enum_values) VALUES (?, ?, ?, ?, ?)",
                dim.getTableId(), dim.getName(), dim.getDescription(),
                dim.getColumnName(), dim.getEnumValues());
    }

    public void deleteDimension(Long id) {
        jdbcTemplate.update("DELETE FROM dimension_definition WHERE id = ?", id);
    }

    // ===== RowMappers =====

    static class TableMetadataRowMapper implements RowMapper<TableMetadata> {
        @Override
        public TableMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            TableMetadata t = new TableMetadata();
            t.setId(rs.getLong("id"));
            t.setDatasourceId(rs.getLong("datasource_id"));
            t.setTableName(rs.getString("table_name"));
            t.setDisplayName(rs.getString("display_name"));
            t.setDescription(rs.getString("description"));
            Timestamp syncTs = rs.getTimestamp("sync_time");
            if (syncTs != null) t.setSyncTime(syncTs.toLocalDateTime());
            return t;
        }
    }

    static class ColumnMetadataRowMapper implements RowMapper<ColumnMetadata> {
        @Override
        public ColumnMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
            ColumnMetadata c = new ColumnMetadata();
            c.setId(rs.getLong("id"));
            c.setTableId(rs.getLong("table_id"));
            c.setColumnName(rs.getString("column_name"));
            c.setDisplayName(rs.getString("display_name"));
            c.setDataType(rs.getString("data_type"));
            c.setRole(rs.getString("role"));
            c.setDescription(rs.getString("description"));
            c.setEnumValues(rs.getString("enum_values"));
            return c;
        }
    }

    static class MetricRowMapper implements RowMapper<MetricDefinition> {
        @Override
        public MetricDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
            MetricDefinition m = new MetricDefinition();
            m.setId(rs.getLong("id"));
            m.setTableId(rs.getLong("table_id"));
            m.setName(rs.getString("name"));
            m.setDescription(rs.getString("description"));
            m.setExpression(rs.getString("expression"));
            m.setAggFunction(rs.getString("agg_function"));
            m.setUnit(rs.getString("unit"));
            return m;
        }
    }

    static class DimensionRowMapper implements RowMapper<DimensionDefinition> {
        @Override
        public DimensionDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
            DimensionDefinition d = new DimensionDefinition();
            d.setId(rs.getLong("id"));
            d.setTableId(rs.getLong("table_id"));
            d.setName(rs.getString("name"));
            d.setDescription(rs.getString("description"));
            d.setColumnName(rs.getString("column_name"));
            d.setEnumValues(rs.getString("enum_values"));
            return d;
        }
    }
}
