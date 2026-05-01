package com.ankur.loganalyzer.dto;

import java.time.Instant;
import java.util.Map;

public record AlertStatsResponse(
        long totalRules,
        long enabledRules,
        long activeAlerts,
        long resolvedAlerts,
        Map<String, Long> topFiringRules,
        long windowMinutes,
        Instant windowStart,
        Instant windowEnd
) {}
