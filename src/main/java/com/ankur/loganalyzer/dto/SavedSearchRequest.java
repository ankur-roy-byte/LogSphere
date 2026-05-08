package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.validation.ValidLogLevel;
import com.ankur.loganalyzer.validation.ValidTraceId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;

@Builder
public record SavedSearchRequest(
        @NotBlank(message = "Saved search name is required")
        @Size(max = 120, message = "Saved search name cannot exceed 120 characters")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

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

        Instant endTime
) {
}
