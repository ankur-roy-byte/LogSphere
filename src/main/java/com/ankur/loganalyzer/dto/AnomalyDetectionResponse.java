package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record AnomalyDetectionResponse(
        List<TimeSeriesAnomaly> timeSeriesAnomalies,
        List<ServiceAnomaly> serviceAnomalies,
        StatisticalSummary statistics,
        Instant windowStart,
        Instant windowEnd,
        Instant analysisTime
) {
    @Builder
    public record TimeSeriesAnomaly(
            Instant timestamp,
            long actualValue,
            double expectedValue,
            double zScore,
            String type
    ) {}

    @Builder
    public record ServiceAnomaly(
            String serviceName,
            long currentCount,
            long baselineCount,
            double changePercent,
            String type
    ) {}

    @Builder
    public record StatisticalSummary(
            long min,
            long max,
            double mean,
            double median,
            double stdDev
    ) {}
}
