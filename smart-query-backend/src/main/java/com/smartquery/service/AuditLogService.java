package com.smartquery.service;

import com.smartquery.model.QueryAuditLog;
import com.smartquery.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logQuery(String question, String sql, Long executionTime, Integer resultCount,
                         boolean success, String errorMessage, HttpServletRequest request) {
        QueryAuditLog log = new QueryAuditLog();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            log.setUserId(userId);
        }

        if (request != null) {
            log.setIpAddress(getClientIpAddress(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }

        log.setQuestion(question);
        log.setGeneratedSql(sql);
        log.setExecutionTimeMs(executionTime);
        log.setResultCount(resultCount);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);

        auditLogRepository.save(log);
    }

    public List<QueryAuditLog> getMyQueryLogs(int limit) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return auditLogRepository.findByUserId(userId, limit);
        }
        return List.of();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
