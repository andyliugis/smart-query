package com.smartquery.service;

/**
 * LLM Provider interface - abstraction for multiple LLM backends
 */
public interface LlmProvider {
    
    /**
     * Generate SQL from natural language question
     */
    String generateSQL(String question, Long sessionId);
    
    /**
     * Get provider name (e.g., "zhipuai", "openai", "deepseek")
     */
    String getName();
    
    /**
     * Check if this provider is currently available/configured
     */
    boolean isAvailable();
}