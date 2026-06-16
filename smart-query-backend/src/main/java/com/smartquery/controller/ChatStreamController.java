package com.smartquery.controller;

import com.smartquery.service.ChatService;
import com.smartquery.service.LLMService;
import com.smartquery.service.SQLExecutorService;
import com.smartquery.service.AuditLogService;
import com.smartquery.model.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流式聊天控制器 - SSE 实时推送查询进度
 */
@RestController
@RequestMapping("/api/chat")
public class ChatStreamController {

    private static final Logger log = LoggerFactory.getLogger(ChatStreamController.class);

    private final LLMService llmService;
    private final SQLExecutorService sqlExecutorService;
    private final ChatService chatService;
    private final AuditLogService auditLogService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public ChatStreamController(LLMService llmService, SQLExecutorService sqlExecutorService, 
                                ChatService chatService, AuditLogService auditLogService) {
        this.llmService = llmService;
        this.sqlExecutorService = sqlExecutorService;
        this.chatService = chatService;
        this.auditLogService = auditLogService;
    }

    /**
     * SSE 流式查询接口
     * POST /api/chat/stream
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQuery(@RequestBody QueryRequest request) {
        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时

        // 在主线程中获取认证信息，然后传递到子线程
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Long userId = (authentication != null && authentication.getPrincipal() instanceof Long) 
                ? (Long) authentication.getPrincipal() : null;

        executor.execute(() -> {
            // 在子线程中设置认证信息
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            
            try {
                String question = request.getQuestion();
                Long sessionId = request.getSessionId();

                // 1. 发送状态：正在分析
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(Map.of("message", "正在分析您的问题...", "step", "analyzing")));

                // 如果没有会话，创建新会话
                if (sessionId == null) {
                    sessionId = chatService.createSession(null).getId();
                    emitter.send(SseEmitter.event()
                            .name("session")
                            .data(Map.of("sessionId", sessionId)));
                }

                // 保存用户消息
                chatService.addUserMessage(sessionId, question);

                // 2. 生成 SQL
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(Map.of("message", "正在生成 SQL...", "step", "generating_sql")));

                String sql = llmService.generateSQL(question, sessionId);
                if (sql.isEmpty()) {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("message", "无法理解您的问题，请尝试换一种表述")));
                    emitter.complete();
                    return;
                }

                // 3. 发送生成的 SQL
                emitter.send(SseEmitter.event()
                        .name("sql")
                        .data(Map.of("sql", sql)));

                // 4. 获取执行计划
                List<Map<String, Object>> executionPlan = null;
                try {
                    executionPlan = sqlExecutorService.getExecutionPlan(sql);
                    if (executionPlan != null) {
                        emitter.send(SseEmitter.event()
                                .name("execution_plan")
                                .data(Map.of("plan", executionPlan)));
                    }
                } catch (Exception e) {
                    log.warn("获取执行计划失败: {}", e.getMessage());
                }

                // 5. 执行 SQL
                emitter.send(SseEmitter.event()
                        .name("status")
                        .data(Map.of("message", "正在执行查询...", "step", "executing")));

                Map<String, Object> result = sqlExecutorService.executeQuery(sql);

                @SuppressWarnings("unchecked")
                List<String> columns = (List<String>) result.get("columns");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");

                // 6. 发送查询结果
                emitter.send(SseEmitter.event()
                        .name("result")
                        .data(Map.of(
                                "columns", columns,
                                "data", data,
                                "rowCount", data.size()
                        )));

                // 7. 生成解释说明
                String explanation = String.format("根据您的提问「%s」，我生成了以下 SQL 查询，共返回 %d 条结果。",
                        question, data.size());

                emitter.send(SseEmitter.event()
                        .name("explanation")
                        .data(Map.of("explanation", explanation)));

                // 保存助手消息
                chatService.addAssistantMessage(sessionId, explanation, sql, data);

                // 8. 发送完成信号
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(Map.of(
                                "success", true,
                                "sql", sql,
                                "columns", columns,
                                "data", data,
                                "explanation", explanation,
                                "executionPlan", executionPlan != null ? executionPlan : List.of(),
                                "sessionId", sessionId
                        )));

                // 记录审计日志
                auditLogService.logQuery(question, sql, 0L, data.size(), true, null, null);

                emitter.complete();

            } catch (Exception e) {
                log.error("流式查询失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("message", "查询失败: " + e.getMessage())));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });

        emitter.onError(e -> {
            log.error("SSE 连接错误", e);
        });

        return emitter;
    }
}
