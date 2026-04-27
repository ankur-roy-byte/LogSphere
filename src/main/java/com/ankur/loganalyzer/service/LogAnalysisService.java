package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.annotation.MetricCategory;
import com.ankur.loganalyzer.annotation.Tracked;
import com.ankur.loganalyzer.analyzer.AggregationService;
import com.ankur.loganalyzer.analyzer.AnomalyDetector;
import com.ankur.loganalyzer.analyzer.PatternDetector;
import com.ankur.loganalyzer.dto.AggregationResponse;
import com.ankur.loganalyzer.dto.AnalysisSummaryResponse;
import com.ankur.loganalyzer.dto.AnomalyDetectionResponse;
import com.ankur.loganalyzer.dto.PatternAnalysisResponse;
import com.ankur.loganalyzer.dto.SpikeDetectionResponse;
import com.ankur.loganalyzer.model.AnalysisResult;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogAnalysisService {

    private final ParsedLogEventRepository parsedLogEventRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final AggregationService aggregationService;
    private final AnomalyDetector anomalyDetector;
    private final PatternDetector patternDetector;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "logsphere:analysis:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Tracked(category = MetricCategory.ANALYSIS, operation = "summary")
    public AnalysisSummaryResponse generateSummary(Instant windowStart, Instant windowEnd) {
        if (windowStart == null) windowStart = Instant.now().minus(1, ChronoUnit.HOURS);
        if (windowEnd == null) windowEnd = Instant.now();

        // Try cache first
        String cacheKey = CACHE_PREFIX + "summary:" + windowStart.toEpochMilli() + ":" + windowEnd.toEpochMilli();
        AnalysisSummaryResponse cached = getCached(cacheKey, AnalysisSummaryResponse.class);
        if (cached != null) {
            log.debug("Returning cached summary");
            return cached;
        }

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

        AnalysisSummaryResponse response = AnalysisSummaryResponse.builder()
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

        // Cache the result
        setCache(cacheKey, response);

        return response;
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

    public PatternAnalysisResponse analyzePatterns(Instant start, Instant end, int limit) {
        if (start == null) start = Instant.now().minus(1, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();

        List<ParsedLogEvent> events = parsedLogEventRepository.findByTimestampBetween(start, end);
        List<PatternDetector.PatternOccurrence> patterns = patternDetector.findTopPatterns(events, limit);

        List<PatternAnalysisResponse.PatternOccurrence> patternDtos = patterns.stream()
                .map(p -> PatternAnalysisResponse.PatternOccurrence.builder()
                        .pattern(p.getPattern())
                        .count(p.getCount())
                        .exampleMessage(p.getExampleMessage())
                        .build())
                .toList();

        return PatternAnalysisResponse.builder()
                .topPatterns(patternDtos)
                .totalPatternsFound(patternDetector.groupByPattern(events).size())
                .windowStart(start)
                .windowEnd(end)
                .analysisTime(Instant.now())
                .build();
    }

    @Tracked(category = MetricCategory.ANALYSIS, operation = "anomalies")
    public AnomalyDetectionResponse detectAnomalies(Instant start, Instant end, int windowSize) {
        if (start == null) start = Instant.now().minus(24, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();

        List<ParsedLogEvent> events = parsedLogEventRepository.findByLevelAndTimestampBetween(
                ParsedLogEvent.LogLevel.ERROR, start, end);

        // Time series aggregation for anomaly detection
        Map<Instant, Long> timeSeries = aggregationService.aggregateByTimeBucket(events, 5);

        // Z-score anomalies
        List<AnomalyDetector.Anomaly> zScoreAnomalies = anomalyDetector.detectAnomaliesWithZScore(timeSeries);

        // Spike anomalies
        List<AnomalyDetector.Anomaly> spikeAnomalies = anomalyDetector.detectSpikes(timeSeries, windowSize);

        // Combine and convert to DTOs
        List<AnomalyDetectionResponse.TimeSeriesAnomaly> timeSeriesDtos = new ArrayList<>();
        for (AnomalyDetector.Anomaly a : zScoreAnomalies) {
            timeSeriesDtos.add(AnomalyDetectionResponse.TimeSeriesAnomaly.builder()
                    .timestamp(a.getTimestamp())
                    .actualValue(a.getActualValue())
                    .expectedValue(a.getExpectedValue())
                    .zScore(a.getZScore())
                    .type(a.getType().name())
                    .build());
        }
        for (AnomalyDetector.Anomaly a : spikeAnomalies) {
            if (timeSeriesDtos.stream().noneMatch(ts -> ts.timestamp().equals(a.getTimestamp()))) {
                timeSeriesDtos.add(AnomalyDetectionResponse.TimeSeriesAnomaly.builder()
                        .timestamp(a.getTimestamp())
                        .actualValue(a.getActualValue())
                        .expectedValue(a.getExpectedValue())
                        .zScore(a.getZScore())
                        .type(a.getType().name())
                        .build());
            }
        }

        // Service-level anomalies (compare current vs previous period)
        Instant midPoint = Instant.ofEpochSecond((start.getEpochSecond() + end.getEpochSecond()) / 2);
        Map<String, Long> currentCounts = aggregationService.aggregateByService(
                parsedLogEventRepository.findByLevelAndTimestampBetween(ParsedLogEvent.LogLevel.ERROR, midPoint, end));
        Map<String, Long> baselineCounts = aggregationService.aggregateByService(
                parsedLogEventRepository.findByLevelAndTimestampBetween(ParsedLogEvent.LogLevel.ERROR, start, midPoint));

        List<AnomalyDetector.ServiceAnomaly> serviceAnomalies = anomalyDetector.detectServiceAnomalies(currentCounts, baselineCounts);
        List<AnomalyDetectionResponse.ServiceAnomaly> serviceDtos = serviceAnomalies.stream()
                .map(sa -> AnomalyDetectionResponse.ServiceAnomaly.builder()
                        .serviceName(sa.getServiceName())
                        .currentCount(sa.getCurrentCount())
                        .baselineCount(sa.getBaselineCount())
                        .changePercent(sa.getChangePercent())
                        .type(sa.getType().name())
                        .build())
                .toList();

        // Statistical summary
        AggregationService.StatisticalSummary stats = aggregationService.calculateStatistics(events, 5);
        AnomalyDetectionResponse.StatisticalSummary statsDtos = AnomalyDetectionResponse.StatisticalSummary.builder()
                .min(stats.getMin())
                .max(stats.getMax())
                .mean(stats.getMean())
                .median(stats.getMedian())
                .stdDev(stats.getStdDev())
                .build();

        return AnomalyDetectionResponse.builder()
                .timeSeriesAnomalies(timeSeriesDtos)
                .serviceAnomalies(serviceDtos)
                .statistics(statsDtos)
                .windowStart(start)
                .windowEnd(end)
                .analysisTime(Instant.now())
                .build();
    }

    public AggregationResponse getAggregations(Instant start, Instant end, long bucketSizeMinutes) {
        if (start == null) start = Instant.now().minus(1, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();

        // Try cache first
        String cacheKey = CACHE_PREFIX + "aggregations:" + start.toEpochMilli() + ":" + end.toEpochMilli();
        AggregationResponse cached = getCached(cacheKey, AggregationResponse.class);
        if (cached != null) {
            log.debug("Returning cached aggregations");
            return cached;
        }

        List<ParsedLogEvent> events = parsedLogEventRepository.findByTimestampBetween(start, end);

        Map<String, Long> byService = aggregationService.aggregateByService(events);
        Map<ParsedLogEvent.LogLevel, Long> byLevelEnum = aggregationService.aggregateByLevel(events);
        Map<String, Long> byLevel = byLevelEnum.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        Map<String, Long> byExceptionType = aggregationService.aggregateByExceptionType(events);
        Map<String, Long> byHost = aggregationService.aggregateByHost(events);
        Map<Instant, Long> byTimeBucket = aggregationService.aggregateByTimeBucket(events, bucketSizeMinutes);

        AggregationResponse response = AggregationResponse.builder()
                .byService(byService)
                .byLevel(byLevel)
                .byExceptionType(byExceptionType)
                .byHost(byHost)
                .byTimeBucket(byTimeBucket)
                .totalLogs(events.size())
                .windowStart(start)
                .windowEnd(end)
                .analysisTime(Instant.now())
                .build();

        // Cache the result
        setCache(cacheKey, response);

        return response;
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

    public List<Object[]> getCountsByLevel(Instant start, Instant end) {
        if (start == null) start = Instant.now().minus(24, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();
        return parsedLogEventRepository.countByLevelInWindow(start, end);
    }

    public List<Object[]> getCountsByService(Instant start, Instant end) {
        if (start == null) start = Instant.now().minus(24, ChronoUnit.HOURS);
        if (end == null) end = Instant.now();
        return parsedLogEventRepository.countByServiceInWindow(start, end);
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

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return objectMapper.convertValue(cached, type);
            }
        } catch (Exception e) {
            log.warn("Failed to get from cache: {}", key, e);
        }
        return null;
    }

    private void setCache(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to set cache: {}", key, e);
        }
    }
}
