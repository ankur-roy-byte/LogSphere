package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.model.AuditLog;
import lombok.Builder;

import java.time.Instant;

/**
 * DTO for exposing audit log information via API
 */
@Builder
public record AuditLogResponse(
        Long id,
        Instant timestamp,
        String userId,
        String actionType,
        String entityType,
        String entityId,
        String description,
        String status,
        String httpMethod,
        String endpoint,
        String clientIp,
        Long durationMs
) {
    /**
     * Convert AuditLog entity to DTO
     */
    public static AuditLogResponse from(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .timestamp(auditLog.getTimestamp())
                .userId(auditLog.getUserId())
                .actionType(auditLog.getActionType())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .description(auditLog.getDescription())
                .status(auditLog.getStatus())
                .httpMethod(auditLog.getHttpMethod())
                .endpoint(auditLog.getEndpoint())
                .clientIp(auditLog.getClientIp())
                .durationMs(auditLog.getDurationMs())
                .build();
    }
}
