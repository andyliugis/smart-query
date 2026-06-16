package com.smartquery.controller;

import com.smartquery.model.DatasourceConfig;
import com.smartquery.service.DatasourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasources")
public class DatasourceController {

    private final DatasourceService datasourceService;

    public DatasourceController(DatasourceService datasourceService) {
        this.datasourceService = datasourceService;
    }

    @GetMapping
    public ResponseEntity<List<DatasourceConfig>> list() {
        return ResponseEntity.ok(datasourceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasourceConfig> getById(@PathVariable Long id) {
        return ResponseEntity.ok(datasourceService.findById(id));
    }

    @PostMapping
    public ResponseEntity<DatasourceConfig> create(@RequestBody DatasourceConfig config) {
        return ResponseEntity.ok(datasourceService.save(config));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DatasourceConfig> update(@PathVariable Long id, @RequestBody DatasourceConfig config) {
        config.setId(id);
        return ResponseEntity.ok(datasourceService.save(config));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        datasourceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody DatasourceConfig config) {
        return ResponseEntity.ok(datasourceService.testConnection(config));
    }

    @GetMapping("/{id}/tables")
    public ResponseEntity<List<String>> getTableNames(@PathVariable Long id) {
        return ResponseEntity.ok(datasourceService.getTableNames(id));
    }
}