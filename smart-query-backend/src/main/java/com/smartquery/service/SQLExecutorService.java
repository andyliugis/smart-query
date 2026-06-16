package com.smartquery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * SQL 执行引擎 - 安全执行生成的 SQL 并返回结果
 */
@Service
public class SQLExecutorService {

    private static final Logger log = LoggerFactory.getLogger(SQLExecutorService.class);

    private final JdbcTemplate jdbcTemplate;

    public SQLExecutorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 安全执行 SQL 查询并返回结果
     *
     * @param sql 要执行的 SQL 语句
     * @return 包含列名和数据的 Map
     */
    public Map<String, Object> executeQuery(String sql) {
        // 安全检查: 只允许 SELECT 语句
        validateSQL(sql);

        log.info("执行 SQL: {}", sql);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        List<String> columns = new ArrayList<>();
        if (!rows.isEmpty()) {
            columns = new ArrayList<>(rows.get(0).keySet());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("columns", columns);
        result.put("data", rows);
        result.put("rowCount", rows.size());

        return result;
    }

    /**
     * 获取 SQL 执行计划
     *
     * @param sql 要分析的 SQL 语句
     * @return 执行计划的列表
     */
    public List<Map<String, Object>> getExecutionPlan(String sql) {
        // 安全检查: 只允许 SELECT 语句
        validateSQL(sql);

        log.info("获取执行计划: {}", sql);

        // H2 数据库的 EXPLAIN 语法
        String explainSql = "EXPLAIN " + sql;

        List<Map<String, Object>> plan = jdbcTemplate.queryForList(explainSql);

        log.info("执行计划: {}", plan);
        return plan;
    }

    /**
     * SQL 安全校验 - 防止注入和危险操作
     */
    private void validateSQL(String sql) {
        String upperSQL = sql.toUpperCase().trim();

        // 只允许 SELECT 查询
        if (!upperSQL.startsWith("SELECT")) {
            throw new SecurityException("仅支持 SELECT 查询语句");
        }

        // 禁止危险关键字
        String[] dangerousKeywords = {"DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "TRUNCATE", "CREATE", "GRANT", "REVOKE"};
        for (String keyword : dangerousKeywords) {
            if (upperSQL.contains(" " + keyword + " ") || upperSQL.startsWith(keyword + " ")) {
                throw new SecurityException("SQL 包含不允许的操作: " + keyword);
            }
        }
    }
}
