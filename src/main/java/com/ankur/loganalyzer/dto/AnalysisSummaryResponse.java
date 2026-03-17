package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record AnalysisSummaryResponse(
        long totalLogs,
        long totalErrors,
        long totalWarnings,
        long totalInfo,
        Map<String, Long> errorsByService,
        List<ExceptionCount> topExceptions,
        Instant windowStart,
        Instant windowEnd,
        Instant generatedAt
) {

    @Builder
    public record ExceptionCount(
            String exceptionType,
            long count
    ) {
    }
}
