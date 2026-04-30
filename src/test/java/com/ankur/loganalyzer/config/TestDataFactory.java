package com.ankur.loganalyzer.config;

import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.dto.LogSearchRequest;
import com.ankur.loganalyzer.dto.LogUploadRequest;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.model.ParsedLogEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating test data objects consistently across integration tests.
 */
public class TestDataFactory {

    public static LogUploadRequest createLogUploadRequest() {
        return LogUploadRequest.builder()
                .sourceName("test-service")
                .format("json")
                .content("{\"level\":\"INFO\",\"service\":\"test-service\",\"message\":\"Test log entry\",\"timestamp\":\"2024-01-01T10:00:00Z\"}")
                .build();
    }

    public static LogUploadRequest createLogUploadRequest(String format, String content) {
        return LogUploadRequest.builder()
                .sourceName("test-service")
                .format(format)
                .content(content)
                .build();
    }

    public static LogSearchRequest createLogSearchRequest() {
        return LogSearchRequest.builder()
                .serviceName("test-service")
                .level("INFO")
                .page(0)
                .size(20)
                .build();
    }

    public static LogSearchRequest createLogSearchRequest(String serviceName, String level) {
        return LogSearchRequest.builder()
                .serviceName(serviceName)
                .level(level)
                .page(0)
                .size(20)
                .build();
    }

    public static AlertRuleRequest createAlertRuleRequest() {
        return AlertRuleRequest.builder()
                .name("Test Alert Rule")
                .description("Test alert rule for integration tests")
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS.name())
                .serviceName("test-service")
                .threshold(100)
                .build();
    }

    public static AlertRuleRequest createAlertRuleRequest(String name, String serviceName) {
        return AlertRuleRequest.builder()
                .name(name)
                .description("Test rule for " + serviceName)
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS.name())
                .serviceName(serviceName)
                .threshold(50)
                .build();
    }

    public static ParsedLogEvent createParsedLogEvent() {
        return createParsedLogEvent("test-service", ParsedLogEvent.LogLevel.INFO, "Test log message");
    }

    public static ParsedLogEvent createParsedLogEvent(String serviceName, ParsedLogEvent.LogLevel level, String message) {
        return ParsedLogEvent.builder()
                .traceId(UUID.randomUUID().toString())
                .serviceName(serviceName)
                .level(level)
                .message(message)
                .timestamp(Instant.now())
                .host("test-host")
                .metadata(Map.of("source", "test-source"))
                .build();
    }

    public static AlertRule createAlertRule() {
        return AlertRule.builder()
                .name("Test Rule")
                .description("Test alert rule")
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS)
                .serviceName("test-service")
                .threshold(100)
                .enabled(true)
                .build();
    }

    public static AlertRule createAlertRule(String name, String serviceName, int threshold) {
        return AlertRule.builder()
                .name(name)
                .description("Test rule: " + name)
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS)
                .serviceName(serviceName)
                .threshold(threshold)
                .enabled(true)
                .build();
    }
}
