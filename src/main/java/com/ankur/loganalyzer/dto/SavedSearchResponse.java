package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record SavedSearchResponse(
        Long id,
        String name,
        String description,
        String serviceName,
        String level,
        String traceId,
        String keyword,
        String host,
        Instant startTime,
        Instant endTime,
        String createdBy,
        Instant createdAt,
        Instant updatedAt
) {
}
