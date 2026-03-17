package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record SpikeDetectionResponse(
        List<Spike> spikes,
        Instant analysisTime
) {

    @Builder
    public record Spike(
            String serviceName,
            String level,
            long currentCount,
            long previousCount,
            double changePercentage,
            Instant windowStart,
            Instant windowEnd
    ) {
    }
}
