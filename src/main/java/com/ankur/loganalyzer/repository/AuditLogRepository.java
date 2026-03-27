package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for AuditLog entity.
 *
 * Provides data access operations for audit log records.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs for a specific user
     */
    Page<AuditLog> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    /**
     * Find audit logs by action type
     */
    Page<AuditLog> findByActionTypeOrderByTimestampDesc(String actionType, Pageable pageable);

    /**
     * Find audit logs by entity type and ID
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    /**
     * Find audit logs within a time range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampRange(@Param("startTime") Instant startTime,
                                        @Param("endTime") Instant endTime,
                                        Pageable pageable);

    /**
     * Find failed audit logs
     */
    Page<AuditLog> findByStatusOrderByTimestampDesc(String status, Pageable pageable);

    /**
     * Find audit logs by user and action type
     */
    Page<AuditLog> findByUserIdAndActionTypeOrderByTimestampDesc(String userId, String actionType, Pageable pageable);

    /**
     * Find audit logs by endpoint
     */
    @Query("SELECT a FROM AuditLog a WHERE a.endpoint = :endpoint ORDER BY a.timestamp DESC")
    Page<AuditLog> findByEndpoint(@Param("endpoint") String endpoint, Pageable pageable);
}
