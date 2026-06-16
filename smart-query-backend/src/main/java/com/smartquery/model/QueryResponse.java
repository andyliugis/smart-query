package com.smartquery.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 查询响应结果
 */
@Data
public class QueryResponse {
    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 生成的 SQL
     */
    private String sql;

    /**
     * 查询结果列名
     */
    private List<String> columns;

    /**
     * 查询结果数据
     */
    private List<Map<String, Object>> data;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * AI 回答的原始文本
     */
    private String explanation;

    /**
     * SQL 执行计划
     */
    private List<Map<String, Object>> executionPlan;
    
    /**
     * 会话ID
     */
    private Long sessionId;

    public static QueryResponse success(String sql, List<String> columns, List<Map<String, Object>> data, String explanation, List<Map<String, Object>> executionPlan, Long sessionId) {
        QueryResponse response = new QueryResponse();
        response.setSuccess(true);
        response.setSql(sql);
        response.setColumns(columns);
        response.setData(data);
        response.setExplanation(explanation);
        response.setExecutionPlan(executionPlan);
        response.setSessionId(sessionId);
        return response;
    }

    public static QueryResponse error(String errorMessage) {
        QueryResponse response = new QueryResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
