package com.smartquery.service;

import com.smartquery.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 服务 - 调用智谱 GLM 将自然语言转为 SQL
 * System Prompt 由 PromptBuilder 从语义层动态构建
 */
@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    private final ChatClient chatClient;
    private final PromptBuilder promptBuilder;
    private final ChatService chatService;

    public LLMService(ChatClient.Builder chatClientBuilder, PromptBuilder promptBuilder, ChatService chatService) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.chatService = chatService;
    }

    /**
     * 将自然语言问题转换为 SQL，每次查询动态获取语义层 Prompt
     */
    public String generateSQL(String question) {
        return generateSQL(question, null);
    }

    /**
     * 将自然语言问题转换为 SQL，支持会话上下文
     */
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
    
    /**
     * 从历史消息构建对话上下文字符串
     */
    private String buildContextString(List<ChatMessage> history) {
        StringBuilder sb = new StringBuilder();
        // 最多保留最近6条消息作为上下文（3轮对话）
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
     * 从历史消息构建对话上下文
     */
    private List<Message> buildContextMessages(List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();
        // 最多保留最近6条消息作为上下文（3轮对话）
        int startIndex = Math.max(0, history.size() - 6);
        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            if ("user".equals(msg.getRole())) {
                messages.add(new UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                // 助手消息包含SQL和解释
                String content = "我生成的 SQL 是:\n```sql\n" + msg.getGeneratedSql() + "\n```\n";
                if (msg.getContent() != null) {
                    content += msg.getContent();
                }
                messages.add(new AssistantMessage(content));
            }
        }
        return messages;
    }

    /**
     * 清理 LLM 返回的 SQL，去除 markdown 标记等
     */
    private String cleanSQLResponse(String response) {
        if (response == null) {
            return "";
        }
        String sql = response.trim();
        // 去除 ```sql ... ``` 包裹
        if (sql.startsWith("```sql")) {
            sql = sql.substring(6);
        } else if (sql.startsWith("```")) {
            sql = sql.substring(3);
        }
        if (sql.endsWith("```")) {
            sql = sql.substring(0, sql.length() - 3);
        }
        // 去除末尾分号
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql.trim();
    }
}
