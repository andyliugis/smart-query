package com.smartquery.controller;

import com.smartquery.model.QueryRequest;
import com.smartquery.model.QueryResponse;
import com.smartquery.service.AuditLogService;
import com.smartquery.service.ChatService;
import com.smartquery.service.LLMService;
import com.smartquery.service.SQLExecutorService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 查询控制器 - 接收自然语言问题，返回 SQL + 查询结果
 */
@RestController
@RequestMapping("/api")
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private final LLMService llmService;
    private final SQLExecutorService sqlExecutorService;
    private final AuditLogService auditLogService;
    private final ChatService chatService;

    public QueryController(LLMService llmService, SQLExecutorService sqlExecutorService, AuditLogService auditLogService, ChatService chatService) {
        this.llmService = llmService;
        this.sqlExecutorService = sqlExecutorService;
        this.auditLogService = auditLogService;
        this.chatService = chatService;
    }

    /**
     * 智能问数接口
     * POST /api/query
     */
    @PostMapping("/query")
    public QueryResponse query(@RequestBody QueryRequest request, HttpServletRequest httpRequest) {
        long startTime = System.currentTimeMillis();
        String question = request.getQuestion();
        String generatedSql = null;
        boolean success = false;
        String errorMessage = null;
        Integer resultCount = 0;
        Long sessionId = request.getSessionId();

        try {
            if (question == null || question.trim().isEmpty()) {
                errorMessage = "请输入查询问题";
                return QueryResponse.error(errorMessage);
            }

            // 如果没有会话ID，创建新会话
            if (sessionId == null) {
                sessionId = chatService.createSession(null).getId();
            }

            // 保存用户消息
            chatService.addUserMessage(sessionId, question);

            // 1. 调用 LLM 生成 SQL（带上下文）
            generatedSql = llmService.generateSQL(question, sessionId);
            if (generatedSql.isEmpty()) {
                errorMessage = "无法理解您的问题，请尝试换一种表述";
                return QueryResponse.error(errorMessage);
            }

            // 2. 获取执行计划
            List<Map<String, Object>> executionPlan = null;
            try {
                executionPlan = sqlExecutorService.getExecutionPlan(generatedSql);
            } catch (Exception e) {
                log.warn("获取执行计划失败: {}", e.getMessage());
            }

            // 3. 执行 SQL
            Map<String, Object> result = sqlExecutorService.executeQuery(generatedSql);

            @SuppressWarnings("unchecked")
            List<String> columns = (List<String>) result.get("columns");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            resultCount = data.size();

            String explanation = String.format("根据您的提问「%s」，我生成了以下 SQL 查询，共返回 %d 条结果。",
                    question, resultCount);

            // 保存助手消息
            chatService.addAssistantMessage(sessionId, explanation, generatedSql, data);

            success = true;
            long executionTime = System.currentTimeMillis() - startTime;
            auditLogService.logQuery(question, generatedSql, executionTime, resultCount, true, null, httpRequest);

            return QueryResponse.success(generatedSql, columns, data, explanation, executionPlan, sessionId);

        } catch (SecurityException e) {
            log.warn("SQL 安全检查未通过: {}", e.getMessage());
            errorMessage = "查询安全限制: " + e.getMessage();
            return QueryResponse.error(errorMessage);
        } catch (Exception e) {
            log.error("查询执行失败", e);
            errorMessage = "查询失败: " + e.getMessage();
            return QueryResponse.error(errorMessage);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            if (!success) {
                auditLogService.logQuery(question, generatedSql, executionTime, resultCount, false, errorMessage, httpRequest);
            }
        }
    }

    /**
     * 执行用户编辑后的 SQL
     * POST /api/query/execute
     */
    @PostMapping("/query/execute")
    public QueryResponse executeSql(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return QueryResponse.error("SQL 不能为空");
        }

        try {
            // 获取执行计划
            List<Map<String, Object>> executionPlan = null;
            try {
                executionPlan = sqlExecutorService.getExecutionPlan(sql);
            } catch (Exception e) {
                log.warn("获取执行计划失败: {}", e.getMessage());
            }

            // 执行 SQL
            Map<String, Object> result = sqlExecutorService.executeQuery(sql);

            @SuppressWarnings("unchecked")
            List<String> columns = (List<String>) result.get("columns");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");

            String explanation = String.format("执行您编辑的 SQL，共返回 %d 条结果。", data.size());

            return QueryResponse.success(sql, columns, data, explanation, executionPlan, null);

        } catch (SecurityException e) {
            return QueryResponse.error("查询安全限制: " + e.getMessage());
        } catch (Exception e) {
            log.error("SQL 执行失败", e);
            return QueryResponse.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "smart-query");
    }
}
