package com.smartquery.controller;

import com.smartquery.model.LlmProviderConfig;
import com.smartquery.repository.LlmProviderRepository;
import com.smartquery.service.LlmProviderRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for LLM provider management
 */
@RestController
@RequestMapping("/api/llm-providers")
public class LlmProviderController {

    private final LlmProviderRepository repository;
    private final LlmProviderRegistry registry;

    public LlmProviderController(LlmProviderRepository repository, LlmProviderRegistry registry) {
        this.repository = repository;
        this.registry = registry;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listProviders() {
        List<LlmProviderConfig> configs = repository.findAll();
        List<Map<String, Object>> result = configs.stream()
                .map(config -> {
                    var provider = registry.getProvider(config.getName());
                    return Map.<String, Object>of(
                        "id", config.getId(),
                        "name", config.getName(),
                        "displayName", config.getDisplayName(),
                        "modelName", config.getModelName(),
                        "isActive", config.getIsActive(),
                        "available", provider.map(p -> p.isAvailable()).orElse(false)
                    );
                })
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, String>> getActiveProvider() {
        var provider = registry.getActiveProvider();
        return ResponseEntity.ok(Map.of(
                "name", provider.getName(),
                "available", String.valueOf(provider.isAvailable())
        ));
    }

    @PutMapping("/{name}")
    public ResponseEntity<LlmProviderConfig> updateProvider(
            @PathVariable String name,
            @RequestBody LlmProviderConfig config) {
        config.setName(name);
        LlmProviderConfig saved = repository.save(config);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{name}/activate")
    public ResponseEntity<Map<String, String>> activateProvider(@PathVariable String name) {
        // Deactivate all others
        List<LlmProviderConfig> all = repository.findAll();
        for (LlmProviderConfig config : all) {
            config.setIsActive(config.getName().equals(name));
            repository.save(config);
        }
        return ResponseEntity.ok(Map.of("message", "Provider " + name + " activated"));
    }
}