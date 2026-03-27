package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.validation.ValidLogLevel;
import com.ankur.loganalyzer.validation.ValidTraceId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;

@Builder
public record LogSearchRequest(
        @Size(min = 1, max = 100, message = "Service name must be between 1 and 100 characters")
        String serviceName,

        @ValidLogLevel(message = "Invalid log level. Must be one of: TRACE, DEBUG, INFO, WARN, ERROR, FATAL")
        String level,

        @ValidTraceId(message = "Invalid trace ID format")
        String traceId,

        @Size(max = 255, message = "Keyword cannot exceed 255 characters")
        String keyword,

        @Size(min = 1, max = 100, message = "Host name must be between 1 and 100 characters")
        String host,

        Instant startTime,

        Instant endTime,

        @Min(value = 0, message = "Page number cannot be negative")
        int page,

        @Min(value = 1, message = "Page size must be at least 1")
        int size
) {
    public LogSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 20;
    }
}
