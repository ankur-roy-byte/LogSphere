package com.ankur.loganalyzer.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health checker for database connectivity.
 *
 * Verifies that the application can successfully connect to the PostgreSQL database.
 * Reports detailed information about database status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseHealthIndicator {

    private final DataSource dataSource;

    /**
     * Check database health and return status.
     *
     * @return Map containing health status and details
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                result.put("status", "UP");
                result.put("message", "Database is available");
                result.put("database", connection.getMetaData().getDatabaseProductName());
                result.put("version", connection.getMetaData().getDatabaseProductVersion());
            } else {
                result.put("status", "DOWN");
                result.put("message", "Database connection invalid");
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            result.put("status", "DOWN");
            result.put("message", "Database unavailable");
            result.put("error", e.getMessage());
        }
        return result;
    }
}

