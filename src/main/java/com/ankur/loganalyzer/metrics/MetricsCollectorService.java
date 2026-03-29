package com.ankur.loganalyzer.metrics;

import lombok.Getter;
import org.springframework.stereotype.Service;

/**
 * Service for managing and tracking application metrics
 * Singleton that aggregates and exposes application performance data
 */
@Service
@Getter
public class MetricsCollectorService {

    private final ApplicationMetrics metrics = new ApplicationMetrics();

    /**
     * Record log ingestion timing
     */
    public void recordLogIngestion(long durationMs, boolean success) {
        metrics.recordLogIngestion(durationMs, success);
    }

    /**
     * Record log parsing timing
     */
    public void recordLogParsing(long durationMs, boolean success) {
        metrics.recordLogParsing(durationMs, success);
    }

    /**
     * Record analysis execution timing
     */
    public void recordAnalysisExecution(long durationMs, boolean success) {
        metrics.recordAnalysisExecution(durationMs, success);
    }

    /**
     * Record API request
     */
    public void recordApiRequest(long durationMs, boolean success) {
        metrics.recordApiRequest(durationMs, success);
    }

    /**
     * Record cache hit
     */
    public void recordCacheHit() {
        metrics.recordCacheHit();
    }

    /**
     * Record cache miss
     */
    public void recordCacheMiss() {
        metrics.recordCacheMiss();
    }

    /**
     * Record cache eviction
     */
    public void recordCacheEviction() {
        metrics.recordCacheEviction();
    }

    /**
     * Record alert triggered
     */
    public void recordAlertTriggered(boolean sent) {
        metrics.recordAlertTriggered(sent);
    }

    /**
     * Get summary of all metrics
     */
    public MetricsSummary getSummary() {
        return new MetricsSummary(
                metrics.getTotalLogsIngested().get(),
                metrics.getSuccessfulIngestions().get(),
                metrics.getFailedIngestions().get(),
                metrics.getAverageIngestionTimeMs(),
                metrics.getIngestionSuccessRate(),

                metrics.getTotalLogsParsed().get(),
                metrics.getSuccessfulParsing().get(),
                metrics.getFailedParsing().get(),
                metrics.getAverageParsingTimeMs(),
                metrics.getParsingSuccessRate(),

                metrics.getTotalAnalysisJobsExecuted().get(),
                metrics.getSuccessfulAnalysis().get(),
                metrics.getFailedAnalysis().get(),
                metrics.getAverageAnalysisTimeMs(),
                metrics.getAnalysisSuccessRate(),

                metrics.getCacheHits().get(),
                metrics.getCacheMisses().get(),
                metrics.getCacheEvictions().get(),
                metrics.getCacheHitRate(),

                metrics.getTotalAlertsTriggered().get(),
                metrics.getTotalAlertsSent().get(),
                metrics.getTotalAlertsFailed().get(),

                metrics.getTotalApiRequests().get(),
                metrics.getTotalApiErrors().get(),
                metrics.getAverageApiResponseTimeMs(),
                metrics.getApiSuccessRate(),

                metrics.getStartTime()
        );
    }

    /**
     * Reset all metrics (useful for testing)
     */
    public void reset() {
        metrics.getTotalLogsIngested().set(0);
        metrics.getSuccessfulIngestions().set(0);
        metrics.getFailedIngestions().set(0);
        metrics.getTotalIngestionTimeMs().set(0);

        metrics.getTotalLogsParsed().set(0);
        metrics.getSuccessfulParsing().set(0);
        metrics.getFailedParsing().set(0);
        metrics.getTotalParsingTimeMs().set(0);

        metrics.getTotalAnalysisJobsExecuted().set(0);
        metrics.getSuccessfulAnalysis().set(0);
        metrics.getFailedAnalysis().set(0);
        metrics.getTotalAnalysisTimeMs().set(0);

        metrics.getCacheHits().set(0);
        metrics.getCacheMisses().set(0);
        metrics.getCacheEvictions().set(0);

        metrics.getTotalAlertsTriggered().set(0);
        metrics.getTotalAlertsSent().set(0);
        metrics.getTotalAlertsFailed().set(0);

        metrics.getTotalApiRequests().set(0);
        metrics.getTotalApiErrors().set(0);
        metrics.getTotalApiTimeMs().set(0);
    }
}
