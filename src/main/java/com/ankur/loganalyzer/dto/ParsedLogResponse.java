package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record ParsedLogResponse(
        Long id,
        String serviceName,
        String level,
        String message,
        String exceptionType,
        String stackTrace,
        Instant timestamp,
        String traceId,
        String host,
        Map<String, String> metadata
) {
}
