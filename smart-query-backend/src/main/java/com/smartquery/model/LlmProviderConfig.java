package com.smartquery.model;

import java.sql.Timestamp;

/**
 * LLM Provider configuration entity
 */
public class LlmProviderConfig {
    private Long id;
    private String name;
    private String displayName;
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Boolean isActive;
    private String configJson;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public LlmProviderConfig() {}

    public LlmProviderConfig(String name, String displayName, String apiKey, String baseUrl, String modelName, Boolean isActive) {
        this.name = name;
        this.displayName = displayName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.modelName = modelName;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}