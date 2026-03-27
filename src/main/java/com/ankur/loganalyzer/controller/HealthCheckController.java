package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.health.DatabaseHealthIndicator;
import com.ankur.loganalyzer.health.RedisHealthIndicator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check endpoints for monitoring application readiness.
 *
 * Provides endpoints to verify the application's operational status,
 * including database and cache connectivity.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health & Monitoring", description = "Endpoints for application health monitoring and readiness checks")
public class HealthCheckController {

    private final DatabaseHealthIndicator databaseHealth;
    private final RedisHealthIndicator redisHealth;

    @GetMapping
    @Operation(summary = "Get application health",
            description = "Get the overall application health status, including dependencies")
    @ApiResponse(responseCode = "200", description = "Application is healthy")
    @ApiResponse(responseCode = "503", description = "Application or dependencies unhealthy")
    public ResponseEntity<?> getHealth() {
        Map<String, Object> dbHealth = databaseHealth.checkHealth();
        Map<String, Object> redisHealthStatus = redisHealth.checkHealth();

        String overallStatus = "UP".equals(dbHealth.get("status")) && "UP".equals(redisHealthStatus.get("status"))
                ? "UP" : "DOWN";
        int statusCode = "UP".equals(overallStatus) ? 200 : 503;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", overallStatus);
        response.put("database", dbHealth);
        response.put("cache", redisHealthStatus);

        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping("/database")
    @Operation(summary = "Get database health",
            description = "Check database connectivity and status")
    @ApiResponse(responseCode = "200", description = "Database is healthy")
    @ApiResponse(responseCode = "503", description = "Database is unavailable")
    public ResponseEntity<?> getDatabaseHealth() {
        Map<String, Object> health = databaseHealth.checkHealth();
        int statusCode = "UP".equals(health.get("status")) ? 200 : 503;
        return ResponseEntity.status(statusCode).body(health);
    }

    @GetMapping("/redis")
    @Operation(summary = "Get Redis health",
            description = "Check Redis cache connectivity and status")
    @ApiResponse(responseCode = "200", description = "Redis is healthy")
    @ApiResponse(responseCode = "503", description = "Redis is unavailable")
    public ResponseEntity<?> getRedisHealth() {
        Map<String, Object> health = redisHealth.checkHealth();
        int statusCode = "UP".equals(health.get("status")) ? 200 : 503;
        return ResponseEntity.status(statusCode).body(health);
    }

    @GetMapping("/live")
    @Operation(summary = "Kubernetes liveness probe",
            description = "Liveness probe for Kubernetes deployments - indicates if container is running")
    @ApiResponse(responseCode = "200", description = "Container is running")
    public ResponseEntity<Map<String, String>> getLiveness() {
        return ResponseEntity.ok(Map.of("status", "live"));
    }

    @GetMapping("/ready")
    @Operation(summary = "Kubernetes readiness probe",
            description = "Readiness probe for Kubernetes deployments - indicates if service is ready to receive traffic")
    @ApiResponse(responseCode = "200", description = "Service is ready")
    @ApiResponse(responseCode = "503", description = "Service not ready")
    public ResponseEntity<?> getReadiness() {
        Map<String, Object> dbHealth = databaseHealth.checkHealth();
        Map<String, Object> redisHealthStatus = redisHealth.checkHealth();

        boolean isReady = "UP".equals(dbHealth.get("status")) && "UP".equals(redisHealthStatus.get("status"));
        int statusCode = isReady ? 200 : 503;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", isReady ? "ready" : "not_ready");
        response.put("database", dbHealth.get("status"));
        response.put("cache", redisHealthStatus.get("status"));

        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping("/startup")
    @Operation(summary = "Kubernetes startup probe",
            description = "Startup probe for slow-starting containers")
    @ApiResponse(responseCode = "200", description = "Application has started")
    public ResponseEntity<Map<String, String>> getStartup() {
        return ResponseEntity.ok(Map.of("status", "started"));
    }
}
