package com.ankur.loganalyzer.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics for tracking application performance
 * Thread-safe counter and timing metrics
 */
@Getter
@Setter
@AllArgsConstructor
public class ApplicationMetrics {

    // Log ingestion metrics
    private final AtomicLong totalLogsIngested = new AtomicLong(0);
    private final AtomicLong successfulIngestions = new AtomicLong(0);
    private final AtomicLong failedIngestions = new AtomicLong(0);
    private final AtomicLong totalIngestionTimeMs = new AtomicLong(0);

    // Log parsing metrics
    private final AtomicLong totalLogsParsed = new AtomicLong(0);
    private final AtomicLong successfulParsing = new AtomicLong(0);
    private final AtomicLong failedParsing = new AtomicLong(0);
    private final AtomicLong totalParsingTimeMs = new AtomicLong(0);

    // Analysis metrics
    private final AtomicLong totalAnalysisJobsExecuted = new AtomicLong(0);
    private final AtomicLong successfulAnalysis = new AtomicLong(0);
    private final AtomicLong failedAnalysis = new AtomicLong(0);
    private final AtomicLong totalAnalysisTimeMs = new AtomicLong(0);

    // Cache metrics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong cacheEvictions = new AtomicLong(0);

    // Alert metrics
    private final AtomicLong totalAlertsTriggered = new AtomicLong(0);
    private final AtomicLong totalAlertsSent = new AtomicLong(0);
    private final AtomicLong totalAlertsFailed = new AtomicLong(0);

    // API metrics
    private final AtomicLong totalApiRequests = new AtomicLong(0);
    private final AtomicLong totalApiErrors = new AtomicLong(0);
    private final AtomicLong totalApiTimeMs = new AtomicLong(0);

    private final LocalDateTime startTime = LocalDateTime.now();

    // Record ingestion event
    public void recordLogIngestion(long durationMs, boolean success) {
        totalLogsIngested.incrementAndGet();
        totalIngestionTimeMs.addAndGet(durationMs);
        if (success) {
            successfulIngestions.incrementAndGet();
        } else {
            failedIngestions.incrementAndGet();
        }
    }

    // Record parsing event
    public void recordLogParsing(long durationMs, boolean success) {
        totalLogsParsed.incrementAndGet();
        totalParsingTimeMs.addAndGet(durationMs);
        if (success) {
            successfulParsing.incrementAndGet();
        } else {
            failedParsing.incrementAndGet();
        }
    }

    // Record analysis event
    public void recordAnalysisExecution(long durationMs, boolean success) {
        totalAnalysisJobsExecuted.incrementAndGet();
        totalAnalysisTimeMs.addAndGet(durationMs);
        if (success) {
            successfulAnalysis.incrementAndGet();
        } else {
            failedAnalysis.incrementAndGet();
        }
    }

    // Record API request
    public void recordApiRequest(long durationMs, boolean success) {
        totalApiRequests.incrementAndGet();
        totalApiTimeMs.addAndGet(durationMs);
        if (!success) {
            totalApiErrors.incrementAndGet();
        }
    }

    // Record cache operation
    public void recordCacheHit() {
        cacheHits.incrementAndGet();
    }

    public void recordCacheMiss() {
        cacheMisses.incrementAndGet();
    }

    public void recordCacheEviction() {
        cacheEvictions.incrementAndGet();
    }

    // Record alert event
    public void recordAlertTriggered(boolean sent) {
        totalAlertsTriggered.incrementAndGet();
        if (sent) {
            totalAlertsSent.incrementAndGet();
        } else {
            totalAlertsFailed.incrementAndGet();
        }
    }

    // Calculated metrics
    public double getAverageIngestionTimeMs() {
        long total = totalLogsIngested.get();
        return total > 0 ? (double) totalIngestionTimeMs.get() / total : 0;
    }

    public double getAverageParsingTimeMs() {
        long total = totalLogsParsed.get();
        return total > 0 ? (double) totalParsingTimeMs.get() / total : 0;
    }

    public double getAverageAnalysisTimeMs() {
        long total = totalAnalysisJobsExecuted.get();
        return total > 0 ? (double) totalAnalysisTimeMs.get() / total : 0;
    }

    public double getAverageApiResponseTimeMs() {
        long total = totalApiRequests.get();
        return total > 0 ? (double) totalApiTimeMs.get() / total : 0;
    }

    public double getCacheHitRate() {
        long total = cacheHits.get() + cacheMisses.get();
        return total > 0 ? (double) cacheHits.get() / total * 100 : 0;
    }

    public double getIngestionSuccessRate() {
        long total = successfulIngestions.get() + failedIngestions.get();
        return total > 0 ? (double) successfulIngestions.get() / total * 100 : 0;
    }

    public double getParsingSuccessRate() {
        long total = successfulParsing.get() + failedParsing.get();
        return total > 0 ? (double) successfulParsing.get() / total * 100 : 0;
    }

    public double getAnalysisSuccessRate() {
        long total = successfulAnalysis.get() + failedAnalysis.get();
        return total > 0 ? (double) successfulAnalysis.get() / total * 100 : 0;
    }

    public double getApiSuccessRate() {
        long total = totalApiRequests.get();
        return total > 0 ? (double) (total - totalApiErrors.get()) / total * 100 : 0;
    }
}
