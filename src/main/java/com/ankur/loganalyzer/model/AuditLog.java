package com.ankur.loganalyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity for audit logging and compliance tracking.
 *
 * Records all important operations for compliance, security, and debugging purposes.
 * Tracks who did what, when, and with what result.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp DESC"),
        @Index(name = "idx_audit_user_action", columnList = "user_id, action_type"),
        @Index(name = "idx_audit_entity_type", columnList = "entity_type, entity_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Timestamp when the action occurred
     */
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * User ID who performed the action (may be system user)
     */
    @Column(length = 100)
    private String userId;

    /**
     * Type of action: CREATE, UPDATE, DELETE, SEARCH, EXPORT, etc.
     */
    @Column(nullable = false, length = 50)
    private String actionType;

    /**
     * Type of entity affected: ALERT_RULE, LOG_ENTRY, USER, etc.
     */
    @Column(nullable = false, length = 100)
    private String entityType;

    /**
     * ID of the entity affected
     */
    @Column(length = 100)
    private String entityId;

    /**
     * Description of what was done
     */
    @Column(length = 500)
    private String description;

    /**
     * Result status: SUCCESS, FAILURE
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * HTTP method if API action: GET, POST, PUT, DELETE, etc.
     */
    @Column(length = 10)
    private String httpMethod;

    /**
     * API endpoint path if applicable
     */
    @Column(length = 255)
    private String endpoint;

    /**
     * Client IP address
     */
    @Column(length = 50)
    private String clientIp;

    /**
     * Error message if action failed
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Additional context details (JSON format)
     */
    @Column(columnDefinition = "TEXT")
    private String details;

    /**
     * Duration of operation in milliseconds
     */
    private Long durationMs;
}
