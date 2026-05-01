package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.model.LogSource;

import java.time.Instant;

public record LogSourceResponse(
        Long id,
        String name,
        LogSource.SourceType type,
        String configJson,
        boolean active,
        Instant createdAt
) {
    public static LogSourceResponse from(LogSource source) {
        return new LogSourceResponse(
                source.getId(),
                source.getName(),
                source.getType(),
                source.getConfigJson(),
                source.isActive(),
                source.getCreatedAt()
        );
    }
}
