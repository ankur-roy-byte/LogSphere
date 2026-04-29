package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.DashboardSummary;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AlertEventRepository;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ParsedLogEventRepository parsedLogEventRepository;
    private final RawLogEventRepository rawLogEventRepository;
    private final AlertEventRepository alertEventRepository;
    private final AlertRuleRepository alertRuleRepository;
    private final LogSourceRepository logSourceRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public DashboardSummary getSummary() {
        Instant now = Instant.now();
        Instant yesterday = now.minus(24, ChronoUnit.HOURS);

        long errorLogsLast24h = parsedLogEventRepository
                .countByLevelAndTimestampBetween(ParsedLogEvent.LogLevel.ERROR, yesterday, now);

        return new DashboardSummary(
                parsedLogEventRepository.count(),
                rawLogEventRepository.count(),
                alertEventRepository.countByResolvedFalse(),
                alertRuleRepository.count(),
                logSourceRepository.count(),
                analysisResultRepository.count(),
                errorLogsLast24h,
                now
        );
    }
}
