package com.smartquery.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing multiple LLM providers
 */
@Component
public class LlmProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(LlmProviderRegistry.class);

    private final Map<String, LlmProvider> providers = new ConcurrentHashMap<>();

    @Value("${llm.provider.active:zhipuai}")
    private String activeProviderName;

    public LlmProviderRegistry(List<LlmProvider> llmProviders) {
        for (LlmProvider provider : llmProviders) {
            providers.put(provider.getName(), provider);
            log.info("Registered LLM provider: {} (available: {})", provider.getName(), provider.isAvailable());
        }
    }

    /**
     * Get the currently active LLM provider
     */
    public LlmProvider getActiveProvider() {
        return providers.getOrDefault(activeProviderName, getDefaultProvider());
    }

    /**
     * Get a specific LLM provider by name
     */
    public Optional<LlmProvider> getProvider(String name) {
        return Optional.ofNullable(providers.get(name));
    }

    /**
     * Get all registered providers
     */
    public Map<String, LlmProvider> getAllProviders() {
        return Map.copyOf(providers);
    }

    /**
     * Get names of all available providers
     */
    public List<String> getAvailableProviderNames() {
        return providers.values().stream()
                .filter(LlmProvider::isAvailable)
                .map(LlmProvider::getName)
                .toList();
    }

    private LlmProvider getDefaultProvider() {
        return providers.values().stream()
                .filter(LlmProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available LLM provider configured"));
    }
}