package com.ankur.loganalyzer.dto;

import java.time.Instant;
import java.util.Map;

public record LogStatsResponse(
        long totalParsedLogs,
        Map<String, Long> countByLevel,
        Map<String, Long> topServicesByErrorCount,
        Map<String, Long> topServicesByVolume,
        long windowMinutes,
        Instant windowStart,
        Instant windowEnd
) {}
