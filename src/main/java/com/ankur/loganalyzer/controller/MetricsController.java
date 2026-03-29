package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.metrics.MetricsCollectorService;
import com.ankur.loganalyzer.metrics.MetricsSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for exposing application metrics
 * Provides real-time performance and operational metrics
 */
@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "Application performance and operational metrics")
public class MetricsController {

    @Autowired
    private MetricsCollectorService metricsCollectorService;

    @GetMapping
    @Operation(summary = "Get all application metrics", description = "Retrieve comprehensive metrics for ingestion, parsing, analysis, and API performance")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<MetricsSummary>> getAllMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Metrics retrieved successfully"));
    }

    @GetMapping("/ingestion")
    @Operation(summary = "Get ingestion metrics", description = "Retrieve metrics related to log ingestion performance")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Ingestion metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<?>> getIngestionMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(new Object() {
            public final long totalLogsIngested = summary.getTotalLogsIngested();
            public final long successfulIngestions = summary.getSuccessfulIngestions();
            public final long failedIngestions = summary.getFailedIngestions();
            public final double averageTimeMs = summary.getAverageIngestionTimeMs();
            public final double successRate = summary.getIngestionSuccessRate();
        }, "Ingestion metrics"));
    }

    @GetMapping("/parsing")
    @Operation(summary = "Get parsing metrics", description = "Retrieve metrics related to log parsing performance")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Parsing metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<?>> getParsingMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(new Object() {
            public final long totalLogsParsed = summary.getTotalLogsParsed();
            public final long successfulParsing = summary.getSuccessfulParsing();
            public final long failedParsing = summary.getFailedParsing();
            public final double averageTimeMs = summary.getAverageParsingTimeMs();
            public final double successRate = summary.getParsingSuccessRate();
        }, "Parsing metrics"));
    }

    @GetMapping("/analysis")
    @Operation(summary = "Get analysis metrics", description = "Retrieve metrics related to log analysis performance")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Analysis metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<?>> getAnalysisMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(new Object() {
            public final long totalAnalysisJobs = summary.getTotalAnalysisJobsExecuted();
            public final long successfulAnalysis = summary.getSuccessfulAnalysis();
            public final long failedAnalysis = summary.getFailedAnalysis();
            public final double averageTimeMs = summary.getAverageAnalysisTimeMs();
            public final double successRate = summary.getAnalysisSuccessRate();
        }, "Analysis metrics"));
    }

    @GetMapping("/cache")
    @Operation(summary = "Get cache metrics", description = "Retrieve cache hit rates and performance metrics")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Cache metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<?>> getCacheMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(new Object() {
            public final long hits = summary.getCacheHits();
            public final long misses = summary.getCacheMisses();
            public final long evictions = summary.getCacheEvictions();
            public final double hitRate = summary.getCacheHitRate();
        }, "Cache metrics"));
    }

    @GetMapping("/api")
    @Operation(summary = "Get API metrics", description = "Retrieve API request counts, error rates, and response times")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "API metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<?>> getApiMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(new Object() {
            public final long totalRequests = summary.getTotalApiRequests();
            public final long totalErrors = summary.getTotalApiErrors();
            public final double averageResponseTimeMs = summary.getAverageApiResponseTimeMs();
            public final double successRate = summary.getApiSuccessRate();
        }, "API metrics"));
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get alert metrics", description = "Retrieve alert triggering and delivery metrics")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Alert metrics retrieved successfully")
    })
    public ResponseEntity<ApiResponse<?>> getAlertMetrics() {
        MetricsSummary summary = metricsCollectorService.getSummary();
        return ResponseEntity.ok(ApiResponse.success(new Object() {
            public final long totalTriggered = summary.getTotalAlertsTriggered();
            public final long totalSent = summary.getTotalAlertsSent();
            public final long totalFailed = summary.getTotalAlertsFailed();
        }, "Alert metrics"));
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset all metrics", description = "Reset all collected metrics (admin only)")
    @ApiResponses(value = {
            @SwaggerApiResponse(responseCode = "200", description = "Metrics reset successfully")
    })
    public ResponseEntity<ApiResponse<Void>> resetMetrics() {
        metricsCollectorService.reset();
        return ResponseEntity.ok(ApiResponse.success(null, "All metrics have been reset"));
    }
}
