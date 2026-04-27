package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record AggregationResponse(
        Map<String, Long> byService,
        Map<String, Long> byLevel,
        Map<String, Long> byExceptionType,
        Map<String, Long> byHost,
        Map<Instant, Long> byTimeBucket,
        long totalLogs,
        Instant windowStart,
        Instant windowEnd,
        Instant analysisTime
) {}
