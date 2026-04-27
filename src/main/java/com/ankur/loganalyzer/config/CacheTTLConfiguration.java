package com.ankur.loganalyzer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cache TTL (Time-To-Live) configuration properties.
 *
 * Centralizes cache expiration times for different data types,
 * allowing runtime configuration of cache behavior.
 */
@Component
@ConfigurationProperties(prefix = "cache.ttl")
@Getter
@Setter
public class CacheTTLConfiguration {

    /**
     * TTL for alert rules in minutes
     */
    private long alertRules = 30;

    /**
     * TTL for search results in minutes
     */
    private long searchResults = 5;

    /**
     * TTL for log entries in minutes
     */
    private long logEntries = 10;

    /**
     * TTL for analysis results in minutes
     */
    private long analysisResults = 15;

    /**
     * TTL for user sessions in minutes
     */
    private long sessions = 120;

    /**
     * TTL for audit logs in minutes
     */
    private long auditLogs = 60;

    /**
     * Get TTL in seconds for a cache key
     */
    public long getTTLSeconds(String cacheKey) {
        return switch (cacheKey.toLowerCase()) {
            case "alertrules" -> alertRules * 60;
            case "searchresults" -> searchResults * 60;
            case "logentries" -> logEntries * 60;
            case "analysisresults" -> analysisResults * 60;
            case "sessions" -> sessions * 60;
            case "auditlogs" -> auditLogs * 60;
            default -> 300; // Default 5 minutes
        };
    }
}
