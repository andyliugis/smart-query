package com.smartquery.service;

import com.smartquery.model.*;
import com.smartquery.repository.MetadataRepository;
import com.smartquery.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 元数据管理服务 - 自动探测表结构 + 管理语义层
 */
@Service
public class MetadataService {

    private static final Logger log = LoggerFactory.getLogger(MetadataService.class);

    private final MetadataRepository repository;
    private final TagRepository tagRepository;
    private final DataSource dataSource;

    public MetadataService(MetadataRepository repository, TagRepository tagRepository, DataSource dataSource) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.dataSource = dataSource;
    }

    // ===== 元数据同步 =====

    /**
     * 从 JDBC 自动同步表结构到元数据表
     */
    public int syncFromDatabase() {
        int syncCount = 0;
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet tables = dbMeta.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME").toLowerCase();

                syncCount += syncTable(tableName, dbMeta);
            }
            tables.close();
        } catch (SQLException e) {
            log.error("同步元数据失败", e);
            throw new RuntimeException("同步失败: " + e.getMessage(), e);
        }

        log.info("同步完成，共同步 {} 张表", syncCount);
        return syncCount;
    }

    private int syncTable(String tableName, DatabaseMetaData dbMeta) throws SQLException {
        // 检查表是否已存在
        TableMetadata existing = repository.findTableByName(tableName);
        if (existing != null) {
            // 已存在，重新同步字段
            repository.deleteColumnsByTableId(existing.getId());
            syncColumns(existing.getId(), tableName, dbMeta);
            return 1;
        }

        // 新增表
        TableMetadata table = new TableMetadata();
        table.setDatasourceId(1L); // 默认数据源
        table.setTableName(tableName);
        table.setDisplayName(tableName);
        table.setSyncTime(LocalDateTime.now());
        repository.insertTable(table);

        // 查询刚插入的表获取 ID
        TableMetadata inserted = repository.findTableByName(tableName);
        if (inserted != null) {
            syncColumns(inserted.getId(), tableName, dbMeta);
        }
        return 1;
    }

    private void syncColumns(Long tableId, String tableName, DatabaseMetaData dbMeta) throws SQLException {
        ResultSet columns = dbMeta.getColumns(null, null, tableName.toUpperCase(), "%");
        while (columns.next()) {
            ColumnMetadata col = new ColumnMetadata();
            col.setTableId(tableId);
            col.setColumnName(columns.getString("COLUMN_NAME"));
            col.setDisplayName(columns.getString("COLUMN_NAME"));
            col.setDataType(columns.getString("TYPE_NAME") +
                    (columns.getInt("COLUMN_SIZE") > 0 ? "(" + columns.getInt("COLUMN_SIZE") + ")" : ""));
            col.setRole("none"); // 默认无角色，等待人工标注
            col.setDescription(columns.getString("REMARKS"));
            repository.insertColumn(col);
        }
        columns.close();
    }

    // ===== 查询接口 =====

    public List<TableMetadata> getAllTables() {
        List<TableMetadata> tables = repository.findAllTables();
        for (TableMetadata table : tables) {
            table.setColumns(repository.findColumnsByTableId(table.getId()));
            table.setTags(tagRepository.findTagsByTableId(table.getId()));
        }
        return tables;
    }
    
    public List<TableMetadata> getTablesByTag(Long tagId) {
        List<Long> tableIds = tagRepository.findTableIdsByTagId(tagId);
        List<TableMetadata> tables = new ArrayList<>();
        for (Long tableId : tableIds) {
            TableMetadata table = repository.findTableById(tableId);
            if (table != null) {
                table.setColumns(repository.findColumnsByTableId(tableId));
                table.setTags(tagRepository.findTagsByTableId(tableId));
                tables.add(table);
            }
        }
        return tables;
    }

    public List<MetricDefinition> getAllMetrics() {
        return repository.findAllMetrics();
    }

    public List<DimensionDefinition> getAllDimensions() {
        return repository.findAllDimensions();
    }

    // ===== 标注管理 =====

    public void updateColumnAnnotation(ColumnMetadata col) {
        repository.updateColumn(col);
    }

    public void updateTableAnnotation(TableMetadata table) {
        repository.updateTable(table);
    }

    public void addMetric(MetricDefinition metric) {
        repository.insertMetric(metric);
    }

    public void removeMetric(Long id) {
        repository.deleteMetric(id);
    }

    public void addDimension(DimensionDefinition dim) {
        repository.insertDimension(dim);
    }

    public void removeDimension(Long id) {
        repository.deleteDimension(id);
    }
}
