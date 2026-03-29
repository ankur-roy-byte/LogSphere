package com.ankur.loganalyzer.config;

import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.dto.LogSearchRequest;
import com.ankur.loganalyzer.dto.LogUploadRequest;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.model.LogEntry;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory for creating test data objects consistently across integration tests.
 */
public class TestDataFactory {

    public static LogUploadRequest createLogUploadRequest() {
        return LogUploadRequest.builder()
                .source("test-service")
                .logFormat("json")
                .content("{\"level\":\"INFO\",\"message\":\"Test log entry\",\"timestamp\":\"2024-01-01T10:00:00\"}")
                .build();
    }

    public static LogUploadRequest createLogUploadRequest(String format, String content) {
        return LogUploadRequest.builder()
                .source("test-service")
                .logFormat(format)
                .content(content)
                .build();
    }

    public static LogSearchRequest createLogSearchRequest() {
        return LogSearchRequest.builder()
                .serviceName("test-service")
                .logLevel("INFO")
                .pageNumber(1)
                .pageSize(20)
                .build();
    }

    public static LogSearchRequest createLogSearchRequest(String serviceName, String logLevel) {
        return LogSearchRequest.builder()
                .serviceName(serviceName)
                .logLevel(logLevel)
                .pageNumber(1)
                .pageSize(20)
                .build();
    }

    public static AlertRuleRequest createAlertRuleRequest() {
        return AlertRuleRequest.builder()
                .name("Test Alert Rule")
                .description("Test alert rule for integration tests")
                .serviceName("test-service")
                .threshold(100)
                .enabled(true)
                .build();
    }

    public static AlertRuleRequest createAlertRuleRequest(String name, String serviceName) {
        return AlertRuleRequest.builder()
                .name(name)
                .description("Test rule for " + serviceName)
                .serviceName(serviceName)
                .threshold(50)
                .enabled(true)
                .build();
    }

    public static LogEntry createLogEntry() {
        return LogEntry.builder()
                .traceId(UUID.randomUUID().toString())
                .serviceName("test-service")
                .level("INFO")
                .message("Test log message")
                .timestamp(LocalDateTime.now())
                .source("test-source")
                .build();
    }

    public static LogEntry createLogEntry(String serviceName, String level, String message) {
        return LogEntry.builder()
                .traceId(UUID.randomUUID().toString())
                .serviceName(serviceName)
                .level(level)
                .message(message)
                .timestamp(LocalDateTime.now())
                .source("test-source")
                .build();
    }

    public static AlertRule createAlertRule() {
        return AlertRule.builder()
                .name("Test Rule")
                .description("Test alert rule")
                .serviceName("test-service")
                .threshold(100)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static AlertRule createAlertRule(String name, String serviceName, int threshold) {
        return AlertRule.builder()
                .name(name)
                .description("Test rule: " + name)
                .serviceName(serviceName)
                .threshold(threshold)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
