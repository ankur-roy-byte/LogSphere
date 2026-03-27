package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AuditLogResponse;
import com.ankur.loganalyzer.service.AuditLogService;
import com.ankur.loganalyzer.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controller for audit log operations.
 *
 * Exposes endpoints for querying and monitoring audit trails.
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "APIs for accessing and reviewing audit trails for compliance and security")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "Get audit logs",
            description = "Retrieve paginated audit logs with optional filtering")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) String userId,
            @Parameter(description = "Filter by action type") @RequestParam(required = false) String actionType) {

        var pageRequest = PaginationUtils.createPageable(page, size);

        Page<AuditLogResponse> results;
        if (userId != null && actionType != null) {
            results = auditLogService.getAuditLogsByUserAndAction(userId, actionType, pageRequest)
                    .map(AuditLogResponse::from);
        } else if (userId != null) {
            results = auditLogService.getAuditLogsByUser(userId, pageRequest)
                    .map(AuditLogResponse::from);
        } else if (actionType != null) {
            results = auditLogService.getAuditLogsByActionType(actionType, pageRequest)
                    .map(AuditLogResponse::from);
        } else {
            results = auditLogService.getAuditLogsByActionType("ALL", pageRequest)
                    .map(AuditLogResponse::from);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-entity")
    @Operation(summary = "Get audit logs for entity",
            description = "Get all audit logs related to a specific entity")
    @ApiResponse(responseCode = "200", description = "Entity audit logs retrieved successfully")
    public ResponseEntity<?> getAuditLogsForEntity(
            @Parameter(description = "Entity type") @RequestParam String entityType,
            @Parameter(description = "Entity ID") @RequestParam String entityId) {

        var logs = auditLogService.getAuditLogsForEntity(entityType, entityId)
                .stream()
                .map(AuditLogResponse::from)
                .toList();

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-endpoint")
    @Operation(summary = "Get audit logs by endpoint",
            description = "Get all audit logs for a specific API endpoint")
    @ApiResponse(responseCode = "200", description = "Endpoint audit logs retrieved successfully")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByEndpoint(
            @Parameter(description = "Endpoint path") @RequestParam String endpoint,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        var pageRequest = PaginationUtils.createPageable(page, size);

        var results = auditLogService.getAuditLogsByEndpoint(endpoint, pageRequest)
                .map(AuditLogResponse::from);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/failures")
    @Operation(summary = "Get failed operations",
            description = "Get audit logs for operations that failed")
    @ApiResponse(responseCode = "200", description = "Failed operations retrieved successfully")
    public ResponseEntity<Page<AuditLogResponse>> getFailedOperations(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        var pageRequest = PaginationUtils.createPageable(page, size);

        var results = auditLogService.getFailedAuditLogs(pageRequest)
                .map(AuditLogResponse::from);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-time-range")
    @Operation(summary = "Get audit logs by time range",
            description = "Get audit logs within specified time range")
    @ApiResponse(responseCode = "200", description = "Time-range audit logs retrieved successfully")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByTimeRange(
            @Parameter(description = "Start time (ISO 8601)") @RequestParam Instant startTime,
            @Parameter(description = "End time (ISO 8601)") @RequestParam Instant endTime,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        var pageRequest = PaginationUtils.createPageable(page, size);

        var results = auditLogService.getAuditLogsByTimeRange(startTime, endTime, pageRequest)
                .map(AuditLogResponse::from);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get audit log statistics",
            description = "Get overall statistics about audit logs")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<?> getAuditLogStats() {
        long totalCount = auditLogService.getAuditLogCount();
        return ResponseEntity.ok(java.util.Map.of("totalRecords", totalCount));
    }
}
