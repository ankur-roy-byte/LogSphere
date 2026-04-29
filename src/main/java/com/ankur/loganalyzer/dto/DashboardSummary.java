package com.ankur.loganalyzer.dto;

import java.time.Instant;

public record DashboardSummary(
        long totalParsedLogs,
        long totalRawLogs,
        long activeAlerts,
        long totalAlertRules,
        long totalLogSources,
        long totalAnalysisResults,
        long errorLogsLast24h,
        Instant generatedAt
) {}
