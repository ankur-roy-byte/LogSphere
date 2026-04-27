package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AggregationResponse;
import com.ankur.loganalyzer.dto.AnalysisSummaryResponse;
import com.ankur.loganalyzer.dto.AnomalyDetectionResponse;
import com.ankur.loganalyzer.dto.PatternAnalysisResponse;
import com.ankur.loganalyzer.dto.SpikeDetectionResponse;
import com.ankur.loganalyzer.service.LogAnalysisService;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis & Analytics", description = "APIs for log analysis, anomaly detection, and pattern recognition")
public class AnalysisController {

    private final LogAnalysisService logAnalysisService;

    @GetMapping("/summary")
    @Operation(summary = "Get analysis summary",
            description = "Generate comprehensive summary statistics for logs in a time window")
    @ApiResponse(responseCode = "200", description = "Summary generated successfully")
    public ResponseEntity<AnalysisSummaryResponse> getSummary(
            @Parameter(description = "Window start time (ISO-8601)") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time (ISO-8601)") @RequestParam(required = false) Instant endTime) {
        return ResponseEntity.ok(logAnalysisService.generateSummary(startTime, endTime));
    }

    @GetMapping("/errors/top")
    @Operation(summary = "Get top exceptions", description = "List most common exception types")
    @ApiResponse(responseCode = "200", description = "Top exceptions returned")
    public ResponseEntity<List<Map<String, Object>>> getTopExceptions(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Number of results (default: 10)") @RequestParam(defaultValue = "10") int limit) {

        List<Object[]> results = logAnalysisService.getTopExceptions(startTime, endTime, limit);
        List<Map<String, Object>> response = results.stream()
                .map(row -> Map.<String, Object>of(
                        "exceptionType", row[0].toString(),
                        "count", (Long) row[1]))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/errors/by-level")
    @Operation(summary = "Get error counts by level", description = "Aggregate error counts by log level")
    @ApiResponse(responseCode = "200", description = "Counts by level returned")
    public ResponseEntity<List<Map<String, Object>>> getErrorsByLevel(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime) {

        List<Object[]> results = logAnalysisService.getCountsByLevel(startTime, endTime);
        List<Map<String, Object>> response = results.stream()
                .map(row -> Map.<String, Object>of(
                        "level", row[0].toString(),
                        "count", (Long) row[1]))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/errors/by-service")
    @Operation(summary = "Get error counts by service", description = "Aggregate error counts grouped by service name")
    @ApiResponse(responseCode = "200", description = "Counts by service returned")
    public ResponseEntity<List<Map<String, Object>>> getErrorsByService(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime) {

        List<Object[]> results = logAnalysisService.getCountsByService(startTime, endTime);
        List<Map<String, Object>> response = results.stream()
                .map(row -> Map.<String, Object>of(
                        "serviceName", row[0] != null ? row[0].toString() : "unknown",
                        "count", (Long) row[1]))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spikes")
    @Operation(summary = "Detect error spikes",
            description = "Detect sudden increases in error rates compared to previous hour")
    @ApiResponse(responseCode = "200", description = "Spike detection completed")
    public ResponseEntity<SpikeDetectionResponse> detectSpikes() {
        return ResponseEntity.ok(logAnalysisService.detectSpikes());
    }

    @GetMapping("/patterns")
    @Operation(summary = "Analyze patterns", description = "Detect and analyze recurring log patterns")
    @ApiResponse(responseCode = "200", description = "Pattern analysis completed")
    public ResponseEntity<PatternAnalysisResponse> analyzePatterns(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Number of patterns to return") @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(logAnalysisService.analyzePatterns(startTime, endTime, limit));
    }

    @GetMapping("/anomalies")
    @Operation(summary = "Detect anomalies",
            description = "Detect statistical anomalies in log patterns and error rates")
    @ApiResponse(responseCode = "200", description = "Anomaly detection completed")
    public ResponseEntity<AnomalyDetectionResponse> detectAnomalies(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Time window size for analysis (minutes)") @RequestParam(defaultValue = "5") int windowSize) {
        return ResponseEntity.ok(logAnalysisService.detectAnomalies(startTime, endTime, windowSize));
    }

    @GetMapping("/aggregations")
    @Operation(summary = "Get aggregations",
            description = "Get aggregated statistics by service, level, exception type, and time bucket")
    @ApiResponse(responseCode = "200", description = "Aggregations returned")
    public ResponseEntity<AggregationResponse> getAggregations(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Time bucket size in minutes") @RequestParam(defaultValue = "5") long bucketSizeMinutes) {
        return ResponseEntity.ok(logAnalysisService.getAggregations(startTime, endTime, bucketSizeMinutes));
    }

    @GetMapping("/exceptions")
    @Operation(summary = "Get repeated messages",
            description = "Find log messages that repeat frequently")
    @ApiResponse(responseCode = "200", description = "Repeated messages returned")
    public ResponseEntity<List<Map<String, Object>>> getRepeatedMessages(
            @Parameter(description = "Window start time") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "Window end time") @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Minimum occurrence count") @RequestParam(defaultValue = "3") long minCount,
            @Parameter(description = "Maximum results to return") @RequestParam(defaultValue = "20") int limit) {

        List<Object[]> results = logAnalysisService.getRepeatedMessages(startTime, endTime, minCount, limit);
        List<Map<String, Object>> response = results.stream()
                .map(row -> Map.<String, Object>of(
                        "message", row[0].toString(),
                        "count", (Long) row[1]))
                .toList();
        return ResponseEntity.ok(response);
    }
}
