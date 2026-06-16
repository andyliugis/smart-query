package com.smartquery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartquery.model.ChatMessage;
import com.smartquery.model.ChatSession;
import com.smartquery.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        return null;
    }

    public List<ChatSession> getMySessions() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return List.of();
        }
        return chatRepository.findSessionsByUserId(userId);
    }

    public ChatSession createSession(String title) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }

        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "新对话");
        session.setStatus("ACTIVE");

        Long sessionId = chatRepository.createSession(session);
        session.setId(sessionId);
        return session;
    }

    public List<ChatMessage> getSessionMessages(Long sessionId) {
        return chatRepository.findMessagesBySessionId(sessionId, 50); // 最多获取50条历史记录
    }

    public void addUserMessage(Long sessionId, String content) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole("user");
        message.setContent(content);
        chatRepository.createMessage(message);
    }

    public void addAssistantMessage(Long sessionId, String content, String sql, List<Map<String, Object>> data) {
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole("assistant");
        message.setContent(content);
        message.setGeneratedSql(sql);
        
        try {
            message.setResultData(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            message.setResultData(null);
        }
        
        chatRepository.createMessage(message);
        
        // 更新会话的标题（用第一条用户消息作为标题）
        updateSessionTitleFromFirstMessage(sessionId);
    }

    private void updateSessionTitleFromFirstMessage(Long sessionId) {
        List<ChatMessage> messages = chatRepository.findMessagesBySessionId(sessionId, 2);
        if (!messages.isEmpty()) {
            ChatMessage firstMessage = messages.get(0);
            String title = firstMessage.getContent();
            if (title.length() > 50) {
                title = title.substring(0, 50) + "...";
            }
            chatRepository.updateSessionTitle(sessionId, title);
        }
    }
}
