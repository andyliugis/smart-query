package com.smartquery.service;

import com.smartquery.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Zhipuai GLM LLM Provider implementation
 */
@Service
public class ZhipuaiLlmProvider implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(ZhipuaiLlmProvider.class);

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final ChatService chatService;

    @Value("${llm.provider.active:zhipuai}")
    private String activeProvider;

    public ZhipuaiLlmProvider(ChatClient.Builder chatClientBuilder, PromptBuilder promptBuilder, ChatService chatService) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.chatService = chatService;
    }

    @Override
    public String generateSQL(String question, Long sessionId) {
        log.info("用户问题: {}", question);

        // 从语义层动态构建 System Prompt
        String systemPrompt = promptBuilder.buildSystemPrompt();
        
        // 如果有会话，添加上下文到 System Prompt
        if (sessionId != null) {
            List<ChatMessage> history = chatService.getSessionMessages(sessionId);
            String context = buildContextString(history);
            if (!context.isEmpty()) {
                systemPrompt += "\n\n## 对话历史\n" + context;
            }
        }
        
        log.debug("System Prompt:\n{}", systemPrompt);

        // 调用 LLM
        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();

        String sql = cleanSQLResponse(response);
        log.info("生成的 SQL: {}", sql);

        return sql;
    }

    @Override
    public String getName() {
        return "zhipuai";
    }

    @Override
    public boolean isAvailable() {
        return true; // Always available as primary provider
    }
    
    /**
     * 从历史消息构建对话上下文字符串
     */
    private String buildContextString(List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        int startIndex = Math.max(0, history.size() - 6);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            sb.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n");
            if ("assistant".equals(msg.getRole()) && msg.getGeneratedSql() != null) {
                sb.append("生成的 SQL: ").append(msg.getGeneratedSql()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 清理 LLM 返回的 SQL，去除 markdown 标记等
     */
    private String cleanSQLResponse(String response) {
        if (response == null) {
            return "";
        }
        String sql = response.trim();
        if (sql.startsWith("```sql")) {
            sql = sql.substring(6);
        } else if (sql.startsWith("```")) {
            sql = sql.substring(3);
        }
        if (sql.endsWith("```")) {
            sql = sql.substring(0, sql.length() - 3);
        }
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql.trim();
    }
}