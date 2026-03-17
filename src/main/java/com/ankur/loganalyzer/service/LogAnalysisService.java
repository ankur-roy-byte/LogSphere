package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.AnalysisSummaryResponse;
import com.ankur.loganalyzer.dto.SpikeDetectionResponse;
import com.ankur.loganalyzer.model.AnalysisResult;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogAnalysisService {

    private final ParsedLogEventRepository parsedLogEventRepository;
    private final AnalysisResultRepository analysisResultRepository;

    public AnalysisSummaryResponse generateSummary(Instant windowStart, Instant windowEnd) {
        if (windowStart == null) windowStart = Instant.now().minus(1, ChronoUnit.HOURS);
        if (windowEnd == null) windowEnd = Instant.now();

        long totalLogs = parsedLogEventRepository.countByTimestampBetween(windowStart, windowEnd);
        long totalErrors = parsedLogEventRepository.countByLevelAndTimestampBetween(
                ParsedLogEvent.LogLevel.ERROR, windowStart, windowEnd);
        long totalWarnings = parsedLogEventRepository.countByLevelAndTimestampBetween(
                ParsedLogEvent.LogLevel.WARN, windowStart, windowEnd);
        long totalInfo = parsedLogEventRepository.countByLevelAndTimestampBetween(
                ParsedLogEvent.LogLevel.INFO, windowStart, windowEnd);

        // Errors by service
        List<Object[]> errorsByServiceRaw = parsedLogEventRepository.countByServiceAndLevelInWindow(
                ParsedLogEvent.LogLevel.ERROR, windowStart, windowEnd);
        Map<String, Long> errorsByService = new LinkedHashMap<>();
        for (Object[] row : errorsByServiceRaw) {
            String service = row[0] != null ? row[0].toString() : "unknown";
            Long count = (Long) row[1];
            errorsByService.put(service, count);
        }

        // Top exceptions
        List<Object[]> topExceptionsRaw = parsedLogEventRepository.findTopExceptions(
                windowStart, windowEnd, PageRequest.of(0, 10));
        List<AnalysisSummaryResponse.ExceptionCount> topExceptions = topExceptionsRaw.stream()
                .map(row -> AnalysisSummaryResponse.ExceptionCount.builder()
                        .exceptionType(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .toList();

        Instant now = Instant.now();

        // Persist analysis results
        persistAnalysisResult(AnalysisResult.AnalysisType.ERROR_COUNT,
                "total_errors", String.valueOf(totalErrors), windowStart, windowEnd, now);
        persistAnalysisResult(AnalysisResult.AnalysisType.WARNING_COUNT,
                "total_warnings", String.valueOf(totalWarnings), windowStart, windowEnd, now);

        return AnalysisSummaryResponse.builder()
                .totalLogs(totalLogs)
                .totalErrors(totalErrors)
                .totalWarnings(totalWarnings)
                .totalInfo(totalInfo)
                .errorsByService(errorsByService)
                .topExceptions(topExceptions)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .generatedAt(now)
                .build();
    }

    public SpikeDetectionResponse detectSpikes() {
        Instant now = Instant.now();
        Instant currentStart = now.minus(1, ChronoUnit.HOURS);
        Instant previousStart = currentStart.minus(1, ChronoUnit.HOURS);

        List<Object[]> currentErrors = parsedLogEventRepository.countByServiceAndLevelInWindow(
                ParsedLogEvent.LogLevel.ERROR, currentStart, now);
        List<Object[]> previousErrors = parsedLogEventRepository.countByServiceAndLevelInWindow(
                ParsedLogEvent.LogLevel.ERROR, previousStart, currentStart);

        Map<String, Long> currentMap = new LinkedHashMap<>();
        for (Object[] row : currentErrors) {
            currentMap.put(row[0] != null ? row[0].toString() : "unknown", (Long) row[1]);
        }

        Map<String, Long> previousMap = new LinkedHashMap<>();
        for (Object[] row : previousErrors) {
            previousMap.put(row[0] != null ? row[0].toString() : "unknown", (Long) row[1]);
        }

        List<SpikeDetectionResponse.Spike> spikes = new ArrayList<>();
        for (Map.Entry<String, Long> entry : currentMap.entrySet()) {
            String service = entry.getKey();
            long current = entry.getValue();
            long previous = previousMap.getOrDefault(service, 0L);

            if (previous > 0) {
                double changePercent = ((double) (current - previous) / previous) * 100;
                if (changePercent > 50) { // 50% increase threshold
                    spikes.add(SpikeDetectionResponse.Spike.builder()
                            .serviceName(service)
                            .level("ERROR")
                            .currentCount(current)
                            .previousCount(previous)
                            .changePercentage(changePercent)
                            .windowStart(currentStart)
                            .windowEnd(now)
                            .build());
                }
            } else if (current > 5) {
                // New errors with no previous baseline
                spikes.add(SpikeDetectionResponse.Spike.builder()
                        .serviceName(service)
                        .level("ERROR")
                        .currentCount(current)
                        .previousCount(0)
                        .changePercentage(100.0)
                        .windowStart(currentStart)
                        .windowEnd(now)
                        .build());
            }
        }

        return SpikeDetectionResponse.builder()
                .spikes(spikes)
                .analysisTime(now)
                .build();
    }

    public List<Object[]> getTopExceptions(Instant start, Instant end, int limit) {
        if (start == null) start = Instant.now().minus(24, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();
        return parsedLogEventRepository.findTopExceptions(start, end, PageRequest.of(0, limit));
    }

    public List<Object[]> getRepeatedMessages(Instant start, Instant end, long minCount, int limit) {
        if (start == null) start = Instant.now().minus(24, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();
        return parsedLogEventRepository.findRepeatedMessages(start, end, minCount, PageRequest.of(0, limit));
    }

    private void persistAnalysisResult(AnalysisResult.AnalysisType type, String key, String value,
                                       Instant windowStart, Instant windowEnd, Instant generatedAt) {
        analysisResultRepository.save(AnalysisResult.builder()
                .analysisType(type)
                .resultKey(key)
                .resultValue(value)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .generatedAt(generatedAt)
                .build());
    }
}
