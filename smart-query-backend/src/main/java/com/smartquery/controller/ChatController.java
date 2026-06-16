package com.smartquery.controller;

import com.smartquery.model.ChatMessage;
import com.smartquery.model.ChatSession;
import com.smartquery.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping("/sessions")
    public List<ChatSession> getSessions() {
        return chatService.getMySessions();
    }

    /**
     * 创建新会话
     */
    @PostMapping("/sessions")
    public ChatSession createSession(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        return chatService.createSession(title);
    }

    /**
     * 获取会话的消息历史
     */
    @GetMapping("/sessions/{id}/messages")
    public List<ChatMessage> getSessionMessages(@PathVariable Long id) {
        return chatService.getSessionMessages(id);
    }
}
