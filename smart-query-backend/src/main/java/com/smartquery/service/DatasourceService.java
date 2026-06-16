package com.smartquery.service;

import com.smartquery.model.DatasourceConfig;
import com.smartquery.repository.DatasourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatasourceService {

    private static final Logger log = LoggerFactory.getLogger(DatasourceService.class);

    private final DatasourceRepository repository;
    private final Map<Long, javax.sql.DataSource> datasourceCache = new ConcurrentHashMap<>();

    public DatasourceService(DatasourceRepository repository) {
        this.repository = repository;
    }

    public List<DatasourceConfig> findAll() {
        return repository.findAll();
    }

    public DatasourceConfig findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("数据源不存在: " + id));
    }

    public DatasourceConfig save(DatasourceConfig config) {
        DatasourceConfig saved = repository.save(config);
        datasourceCache.remove(saved.getId());
        return saved;
    }

    public void delete(Long id) {
        repository.deleteById(id);
        datasourceCache.remove(id);
    }

    /**
     * Test database connection
     */
    public Map<String, Object> testConnection(DatasourceConfig config) {
        long startTime = System.currentTimeMillis();
        try {
            Class.forName(getDriverClass(config.getDbType()));
            try (Connection conn = DriverManager.getConnection(
                    config.getJdbcUrl(), config.getUsername(), config.getPassword());
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {
                long elapsed = System.currentTimeMillis() - startTime;
                if (rs.next()) {
                    return Map.of(
                        "success", true,
                        "message", "连接成功",
                        "elapsedMs", elapsed
                    );
                }
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("数据源连接测试失败: {}", e.getMessage());
            return Map.of(
                "success", false,
                "message", "连接失败: " + e.getMessage(),
                "elapsedMs", elapsed
            );
        }
        return Map.of("success", false, "message", "未知错误");
    }

    /**
     * Get table list from a datasource
     */
    public List<String> getTableNames(Long datasourceId) {
        DatasourceConfig config = findById(datasourceId);
        try {
            Class.forName(getDriverClass(config.getDbType()));
            try (Connection conn = DriverManager.getConnection(
                    config.getJdbcUrl(), config.getUsername(), config.getPassword());
                 ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
                java.util.List<String> tables = new java.util.ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
                return tables;
            }
        } catch (Exception e) {
            log.error("获取表列表失败: {}", e.getMessage());
            throw new RuntimeException("获取表列表失败: " + e.getMessage());
        }
    }

    private String getDriverClass(String dbType) {
        return switch (dbType.toUpperCase()) {
            case "MYSQL" -> "com.mysql.cj.jdbc.Driver";
            case "POSTGRESQL", "POSTGRES" -> "org.postgresql.Driver";
            case "CLICKHOUSE" -> "com.clickhouse.jdbc.ClickHouseDriver";
            case "H2" -> "org.h2.Driver";
            default -> "org.h2.Driver";
        };
    }
}