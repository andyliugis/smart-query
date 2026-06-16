package com.smartquery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * LLM 服务 - 委托给当前活跃的 LLM Provider
 * 保持向后兼容性，控制器无需修改
 */
@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    private final LlmProviderRegistry providerRegistry;

    public LLMService(LlmProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    /**
     * 将自然语言问题转换为 SQL，使用当前活跃的 LLM Provider
     */
    public String generateSQL(String question) {
        return generateSQL(question, null);
    }

    /**
     * 将自然语言问题转换为 SQL，支持会话上下文
     */
    public String generateSQL(String question, Long sessionId) {
        LlmProvider provider = providerRegistry.getActiveProvider();
        log.info("使用 LLM Provider: {} 生成 SQL", provider.getName());
        return provider.generateSQL(question, sessionId);
    }

    /**
     * 获取当前活跃的 LLM Provider 名称
     */
    public String getActiveProviderName() {
        return providerRegistry.getActiveProvider().getName();
    }
}
