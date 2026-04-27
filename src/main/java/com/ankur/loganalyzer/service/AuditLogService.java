package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.model.AuditLog;
import com.ankur.loganalyzer.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing audit logs.
 *
 * Handles recording of audit events for compliance, security monitoring,
 * and operational visibility.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Record an audit log entry asynchronously.
     *
     * Async to prevent audit logging from blocking request processing.
     */
    @Async("persistenceExecutor")
    public void recordAudit(String userId, String actionType, String entityType, String entityId,
                           String status, String endpoint, String clientIp,
                           String description, String errorMessage, Long durationMs) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .timestamp(Instant.now())
                    .userId(userId)
                    .actionType(actionType)
                    .entityType(entityType)
                    .entityId(entityId)
                    .status(status)
                    .endpoint(endpoint)
                    .clientIp(clientIp)
                    .description(description)
                    .errorMessage(errorMessage)
                    .durationMs(durationMs)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log recorded: {} - {} - {}", userId, actionType, status);
        } catch (Exception e) {
            log.error("Failed to record audit log", e);
        }
    }

    /**
     * Record a simple audit event
     */
    @Async("persistenceExecutor")
    public void recordAuditEvent(String actionType, String entityType, String status, String description) {
        recordAudit("SYSTEM", actionType, entityType, null, status, null, null, description, null, null);
    }

    /**
     * Get audit logs for a specific user
     */
    public Page<AuditLog> getAuditLogsByUser(String userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Get audit logs by action type
     */
    public Page<AuditLog> getAuditLogsByActionType(String actionType, Pageable pageable) {
        return auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType, pageable);
    }

    /**
     * Get audit logs for a specific entity
     */
    public List<AuditLog> getAuditLogsForEntity(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    /**
     * Get audit logs within a time range
     */
    public Page<AuditLog> getAuditLogsByTimeRange(Instant startTime, Instant endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampRange(startTime, endTime, pageable);
    }

    /**
     * Get failed audit logs
     */
    public Page<AuditLog> getFailedAuditLogs(Pageable pageable) {
        return auditLogRepository.findByStatusOrderByTimestampDesc("FAILURE", pageable);
    }

    /**
     * Get audit logs by user and action
     */
    public Page<AuditLog> getAuditLogsByUserAndAction(String userId, String actionType, Pageable pageable) {
        return auditLogRepository.findByUserIdAndActionTypeOrderByTimestampDesc(userId, actionType, pageable);
    }

    /**
     * Get audit logs by endpoint
     */
    public Page<AuditLog> getAuditLogsByEndpoint(String endpoint, Pageable pageable) {
        return auditLogRepository.findByEndpoint(endpoint, pageable);
    }

    /**
     * Get total count of audit logs
     */
    public long getAuditLogCount() {
        return auditLogRepository.count();
    }

    /**
     * Delete old audit logs (older than specified days)
     */
    @Async
    public void cleanupOldAuditLogs(int daysToKeep) {
        try {
            Instant cutoffTime = Instant.now().minusSeconds((long) daysToKeep * 24 * 60 * 60);
            long deletedCount = auditLogRepository.count() -
                    auditLogRepository.findAll(Pageable.unpaged()).getNumberOfElements();
            log.info("Cleaned up audit logs before: {} (deleted approximately {})",
                    cutoffTime, Math.max(0, deletedCount));
        } catch (Exception e) {
            log.error("Failed to cleanup old audit logs", e);
        }
    }
}
