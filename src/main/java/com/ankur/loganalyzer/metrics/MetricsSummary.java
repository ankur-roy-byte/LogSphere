package com.ankur.loganalyzer.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Summary of all collected metrics
 * Used for exposing metrics via API or monitoring
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricsSummary {

    // Ingestion metrics
    private long totalLogsIngested;
    private long successfulIngestions;
    private long failedIngestions;
    private double averageIngestionTimeMs;
    private double ingestionSuccessRate;

    // Parsing metrics
    private long totalLogsParsed;
    private long successfulParsing;
    private long failedParsing;
    private double averageParsingTimeMs;
    private double parsingSuccessRate;

    // Analysis metrics
    private long totalAnalysisJobsExecuted;
    private long successfulAnalysis;
    private long failedAnalysis;
    private double averageAnalysisTimeMs;
    private double analysisSuccessRate;

    // Cache metrics
    private long cacheHits;
    private long cacheMisses;
    private long cacheEvictions;
    private double cacheHitRate;

    // Alert metrics
    private long totalAlertsTriggered;
    private long totalAlertsSent;
    private long totalAlertsFailed;

    // API metrics
    private long totalApiRequests;
    private long totalApiErrors;
    private double averageApiResponseTimeMs;
    private double apiSuccessRate;

    // System info
    private LocalDateTime startTime;
}
