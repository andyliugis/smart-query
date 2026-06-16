package com.smartquery.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;

/**
 * User context - carries user identity through the request chain
 */
public class UserContext {

    private final Long userId;
    private final String username;
    private final Map<String, Object> metadata;

    public UserContext(Long userId, String username, Map<String, Object> metadata) {
        this.userId = userId;
        this.username = username;
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
    }

    /**
     * Create UserContext from Spring Security Authentication
     */
    public static UserContext fromAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Long userId = null;
        if (auth.getPrincipal() instanceof Long id) {
            userId = id;
        }
        String username = auth.getName();
        return new UserContext(userId, username, null);
    }

    /**
     * Get current UserContext from SecurityContext
     */
    public static UserContext getCurrent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return fromAuthentication(auth);
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Map<String, Object> getMetadata() { return metadata; }

    public boolean isAuthenticated() {
        return userId != null;
    }
}