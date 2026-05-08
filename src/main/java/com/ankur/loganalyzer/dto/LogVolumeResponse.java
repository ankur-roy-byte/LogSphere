package com.ankur.loganalyzer.dto;

import java.time.Instant;
import java.util.List;

public record LogVolumeResponse(
        List<Bucket> buckets,
        String granularity,
        Instant from,
        Instant to,
        long totalEvents
) {
    public record Bucket(Instant timestamp, long count) {}
}
