package com.smartquery.controller;

import com.smartquery.model.*;
import com.smartquery.service.MetadataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 元数据管理 API - 表结构、指标、维度的 CRUD
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    // ===== 同步 =====

    @PostMapping("/sync")
    public Map<String, Object> sync() {
        int count = metadataService.syncFromDatabase();
        return Map.of("success", true, "syncedTables", count);
    }

    // ===== 表 =====

    @GetMapping("/tables")
    public List<TableMetadata> getTables() {
        return metadataService.getAllTables();
    }

    @PutMapping("/tables/{id}")
    public Map<String, Object> updateTable(@PathVariable Long id, @RequestBody TableMetadata table) {
        table.setId(id);
        metadataService.updateTableAnnotation(table);
        return Map.of("success", true);
    }

    // ===== 字段 =====

    @PutMapping("/columns/{id}")
    public Map<String, Object> updateColumn(@PathVariable Long id, @RequestBody ColumnMetadata col) {
        col.setId(id);
        metadataService.updateColumnAnnotation(col);
        return Map.of("success", true);
    }

    // ===== 指标 =====

    @GetMapping("/metrics")
    public List<MetricDefinition> getMetrics() {
        return metadataService.getAllMetrics();
    }

    @PostMapping("/metrics")
    public Map<String, Object> addMetric(@RequestBody MetricDefinition metric) {
        metadataService.addMetric(metric);
        return Map.of("success", true);
    }

    @DeleteMapping("/metrics/{id}")
    public Map<String, Object> removeMetric(@PathVariable Long id) {
        metadataService.removeMetric(id);
        return Map.of("success", true);
    }

    // ===== 维度 =====

    @GetMapping("/dimensions")
    public List<DimensionDefinition> getDimensions() {
        return metadataService.getAllDimensions();
    }

    @PostMapping("/dimensions")
    public Map<String, Object> addDimension(@RequestBody DimensionDefinition dim) {
        metadataService.addDimension(dim);
        return Map.of("success", true);
    }

    @DeleteMapping("/dimensions/{id}")
    public Map<String, Object> removeDimension(@PathVariable Long id) {
        metadataService.removeDimension(id);
        return Map.of("success", true);
    }
}
