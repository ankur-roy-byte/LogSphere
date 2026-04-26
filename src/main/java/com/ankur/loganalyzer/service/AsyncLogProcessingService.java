package com.ankur.loganalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Async service for long-running log processing operations.
 * Prevents blocking of HTTP threads for heavy computations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncLogProcessingService {

    private final LogAnalysisService analysisService;

    /**
     * Asynchronously process and analyze logs for a service.
     * Returns immediately while processing continues in background.
     */
    @Async("analysisThreadPool")
    public CompletableFuture<String> processLogsAsync(String serviceName, int delayMillis) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                log.info("Starting async log processing for service: {}", serviceName);
                
                if (delayMillis > 0) {
                    Thread.sleep(delayMillis);
                }
                
                var analysis = analysisService.generateSummary(null, null);
                long duration = System.currentTimeMillis() - startTime;

                log.info("Completed async log processing for {} in {}ms", serviceName, duration);
                return String.format("Processed %d logs in %dms",
                    analysis.totalLogs(), duration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Async processing interrupted", e);
            } catch (Exception e) {
                log.error("Error during async log processing", e);
                throw new RuntimeException("Async processing failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Generate comprehensive report asynchronously.
     */
    @Async("analysisThreadPool")
    public CompletableFuture<String> generateReportAsync(String serviceName, String reportType) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                log.info("Generating {} report for service: {}", reportType, serviceName);
                
                // Simulate report generation
                Thread.sleep(2000);
                
                long duration = System.currentTimeMillis() - startTime;
                return String.format("Report generated in %dms", duration);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Report generation interrupted", e);
            } catch (Exception e) {
                log.error("Error generating report", e);
                throw new RuntimeException("Report generation failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Chain multiple async operations.
     */
    @Async("analysisThreadPool")
    public CompletableFuture<String> complexAnalysisWorkflowAsync(String serviceName) {
        return processLogsAsync(serviceName, 0)
            .thenCompose(result -> 
                generateReportAsync(serviceName, "comprehensive")
                    .thenApply(reportResult -> result + " -> " + reportResult)
            )
            .exceptionally(ex -> "Workflow failed: " + ex.getMessage());
    }
}
