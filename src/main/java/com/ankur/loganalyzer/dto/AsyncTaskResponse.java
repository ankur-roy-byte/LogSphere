package com.ankur.loganalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response for async task operations
 * Used to track long-running background tasks
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsyncTaskResponse {

    private String taskId;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    private String taskType; // BULK_ANALYSIS, BULK_IMPORT, REPORT_GENERATION, etc.
    private int progress; // 0-100
    private String message;
    private Object result; // Populated when task completes
    private String errorMessage; // If task fails
    private LocalDateTime startTime;
    private LocalDateTime completionTime;
    private long durationMs; // Time taken to complete
    private long estimatedRemainingMs; // Estimated time to completion
}
