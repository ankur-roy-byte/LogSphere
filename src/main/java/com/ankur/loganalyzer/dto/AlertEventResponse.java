package com.ankur.loganalyzer.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record AlertEventResponse(
        Long id,
        String ruleName,
        String conditionType,
        String message,
        Instant triggeredAt,
        boolean resolved,
        Instant resolvedAt
) {
}
