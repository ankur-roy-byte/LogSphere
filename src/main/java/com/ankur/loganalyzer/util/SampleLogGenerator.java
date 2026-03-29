package com.ankur.loganalyzer.util;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

/**
 * Sample log message generator for testing and demonstration.
 *
 * Generates realistic log messages in various formats with configurable
 * severity levels, services, and anomalies.
 */
public final class SampleLogGenerator {

    private static final Random RANDOM = new Random();

    private static final String[] SERVICES = {
            "api-service", "auth-service", "database-service",
            "cache-service", "notification-service"
    };

    private static final String[] LOG_LEVELS = {"DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

    private static final String[] MESSAGES = {
            "Request processed successfully",
            "Cache hit for key: {}",
            "Database query executed in {}ms",
            "User authentication successful",
            "Invalid authentication token",
            "Connection timeout to external service",
            "Unexpected null value encountered",
            "Rate limit exceeded",
            "Service unavailable - please retry",
            "Configuration reloaded successfully",
            "Memory usage at {}%",
            "Disk space low: {}GB remaining",
            "Backup completed successfully",
            "Health check passed",
            "Scheduled task failed"
    };

    private static final String[] EXCEPTION_TYPES = {
            "NullPointerException",
            "SQLException",
            "TimeoutException",
            "IOException",
            "IllegalArgumentException",
            "RuntimeException"
    };

    private SampleLogGenerator() {
        // Utility class - no instantiation
    }

    /**
     * Generate a random log message in JSON format
     */
    public static String generateJsonLog(String service, String level) {
        String timestamp = Instant.now().toString();
        String traceId = UUID.randomUUID().toString();
        String message = MESSAGES[RANDOM.nextInt(MESSAGES.length)];
        long duration = RANDOM.nextInt(5000);

        return String.format(
                """
                {"timestamp":"%s","service":"%s","level":"%s","traceId":"%s","message":"%s","duration":%d,"thread":"%s","source":"%s"}""",
                timestamp, service, level, traceId, message, duration,
                Thread.currentThread().getName(), "SampleLogGenerator"
        );
    }

    /**
     * Generate a random log message in plain text format
     */
    public static String generatePlainLog(String service, String level) {
        String timestamp = Instant.now().toString();
        String traceId = UUID.randomUUID().toString();
        String message = MESSAGES[RANDOM.nextInt(MESSAGES.length)];

        return String.format("[%s] %s [%s] [%s] %s",
                timestamp, level, service, traceId, message);
    }

    /**
     * Generate a random stack trace for error logs
     */
    public static String generateStackTrace() {
        String exceptionType = EXCEPTION_TYPES[RANDOM.nextInt(EXCEPTION_TYPES.length)];
        StringBuilder sb = new StringBuilder();

        sb.append(exceptionType).append(": ").append("Sample error message\n");
        sb.append("\tat com.ankur.loganalyzer.service.SampleService.process(SampleService.java:42)\n");
        sb.append("\tat com.ankur.loganalyzer.controller.SampleController.handle(SampleController.java:25)\n");
        sb.append("\tat java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:193)\n");
        sb.append("\tat java.util.ArrayList.forEach(ArrayList.java:1257)\n");

        return sb.toString();
    }

    /**
     * Generate random sample logs with anomalies (for testing alerting)
     */
    public static String generateAnomalousLog() {
        String service = SERVICES[RANDOM.nextInt(SERVICES.length)];
        String level = LOG_LEVELS[RANDOM.nextInt(LOG_LEVELS.length)];

        // 30% chance of error-level log
        if (RANDOM.nextInt(100) < 30) {
            level = "ERROR";
        }

        // 10% chance of including exception
        if (RANDOM.nextInt(100) < 10) {
            return generatePlainLog(service, level) + "\n" + generateStackTrace();
        }

        return generateJsonLog(service, level);
    }

    /**
     * Generate bulk sample logs for load testing
     */
    public static String generateBulkLogs(int count) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; i++) {
            String service = SERVICES[RANDOM.nextInt(SERVICES.length)];
            String level = LOG_LEVELS[RANDOM.nextInt(LOG_LEVELS.length)];
            sb.append(generateJsonLog(service, level)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Get a random service name
     */
    public static String getRandomService() {
        return SERVICES[RANDOM.nextInt(SERVICES.length)];
    }

    /**
     * Get a random log level
     */
    public static String getRandomLevel() {
        return LOG_LEVELS[RANDOM.nextInt(LOG_LEVELS.length)];
    }
}
