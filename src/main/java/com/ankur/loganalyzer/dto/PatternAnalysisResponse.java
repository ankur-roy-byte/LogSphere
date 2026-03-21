package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record PatternAnalysisResponse(
        List<PatternOccurrence> topPatterns,
        int totalPatternsFound,
        Instant windowStart,
        Instant windowEnd,
        Instant analysisTime
) {
    @Builder
    public record PatternOccurrence(
            String pattern,
            int count,
            String exampleMessage
    ) {}
}
