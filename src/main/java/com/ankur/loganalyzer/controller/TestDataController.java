package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.util.SampleLogGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Test data generation endpoints for development and testing.
 *
 * Provides endpoints to generate sample log data and test scenarios.
 * Useful for manual testing, demos, and load testing.
 */
@RestController
@RequestMapping("/api/dev/test-data")
@RequiredArgsConstructor
@Tag(name = "Test Data", description = "Endpoints for generating test data (dev/testing only)")
public class TestDataController {

    @GetMapping("/sample-log")
    @Operation(summary = "Generate sample log",
            description = "Generate a single sample log message for testing")
    @ApiResponse(responseCode = "200", description = "Sample log generated successfully")
    public ResponseEntity<Map<String, String>> generateSampleLog(
            @Parameter(description = "Service name")
            @RequestParam(defaultValue = "api-service") String service,
            @Parameter(description = "Log format (json or plain)")
            @RequestParam(defaultValue = "json") String format) {

        String log = "json".equalsIgnoreCase(format)
                ? SampleLogGenerator.generateJsonLog(service, "INFO")
                : SampleLogGenerator.generatePlainLog(service, "INFO");

        return ResponseEntity.ok(Map.of(
                "service", service,
                "format", format,
                "log", log
        ));
    }

    @GetMapping("/anomalous-log")
    @Operation(summary = "Generate anomalous log",
            description = "Generate a log with potential anomalies for alert testing")
    @ApiResponse(responseCode = "200", description = "Anomalous log generated successfully")
    public ResponseEntity<Map<String, String>> generateAnomalousLog() {
        return ResponseEntity.ok(Map.of(
                "message", "Anomalous log generated",
                "log", SampleLogGenerator.generateAnomalousLog()
        ));
    }

    @GetMapping("/stack-trace")
    @Operation(summary = "Generate stack trace",
            description = "Generate a sample Java stack trace for testing exception parsing")
    @ApiResponse(responseCode = "200", description = "Stack trace generated successfully")
    public ResponseEntity<Map<String, String>> generateStackTrace() {
        return ResponseEntity.ok(Map.of(
                "message", "Stack trace generated",
                "stackTrace", SampleLogGenerator.generateStackTrace()
        ));
    }

    @GetMapping("/bulk-logs")
    @Operation(summary = "Generate bulk logs",
            description = "Generate multiple sample logs for load testing")
    @ApiResponse(responseCode = "200", description = "Bulk logs generated successfully")
    public ResponseEntity<Map<String, Object>> generateBulkLogs(
            @Parameter(description = "Number of logs to generate (max 10000)")
            @RequestParam(defaultValue = "100") int count) {

        // Limit to prevent resource exhaustion
        count = Math.min(count, 10000);
        String bulkLogs = SampleLogGenerator.generateBulkLogs(count);

        return ResponseEntity.ok(Map.of(
                "message", "Bulk logs generated",
                "count", count,
                "logs", bulkLogs
        ));
    }

    @GetMapping("/random-service")
    @Operation(summary = "Get random service name",
            description = "Get a random service name from the sample data set")
    @ApiResponse(responseCode = "200", description = "Service name returned successfully")
    public ResponseEntity<Map<String, String>> getRandomService() {
        return ResponseEntity.ok(Map.of(
                "serviceName", SampleLogGenerator.getRandomService()
        ));
    }

    @GetMapping("/random-level")
    @Operation(summary = "Get random log level",
            description = "Get a random log level from the sample data set")
    @ApiResponse(responseCode = "200", description = "Log level returned successfully")
    public ResponseEntity<Map<String, String>> getRandomLevel() {
        return ResponseEntity.ok(Map.of(
                "logLevel", SampleLogGenerator.getRandomLevel()
        ));
    }
}
