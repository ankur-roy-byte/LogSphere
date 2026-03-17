package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record LogSearchRequest(
        String serviceName,
        String level,
        String traceId,
        String keyword,
        String host,
        Instant startTime,
        Instant endTime,
        int page,
        int size
) {
    public LogSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
    }
}
