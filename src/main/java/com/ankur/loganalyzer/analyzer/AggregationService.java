package com.ankur.loganalyzer.analyzer;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides aggregation and statistical analysis of log events
 */
@Component
public class AggregationService {

    /**
     * Aggregates log counts by time buckets
     */
    public Map<Instant, Long> aggregateByTimeBucket(List<ParsedLogEvent> events, long bucketSizeMinutes) {
        Map<Instant, Long> buckets = new TreeMap<>();

        for (ParsedLogEvent event : events) {
            Instant bucket = roundToTimeBucket(event.getTimestamp(), bucketSizeMinutes);
            buckets.merge(bucket, 1L, Long::sum);
        }

        return buckets;
    }

    /**
     * Aggregates error counts by service name
     */
    public Map<String, Long> aggregateByService(List<ParsedLogEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getServiceName() != null ? event.getServiceName() : "unknown",
                        Collectors.counting()
                ));
    }

    /**
     * Aggregates log counts by log level
     */
    public Map<ParsedLogEvent.LogLevel, Long> aggregateByLevel(List<ParsedLogEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(
                        ParsedLogEvent::getLevel,
                        Collectors.counting()
                ));
    }

    /**
     * Aggregates exception counts by exception type
     */
    public Map<String, Long> aggregateByExceptionType(List<ParsedLogEvent> events) {
        return events.stream()
                .filter(event -> event.getExceptionType() != null && !event.getExceptionType().isBlank())
                .collect(Collectors.groupingBy(
                        ParsedLogEvent::getExceptionType,
                        Collectors.counting()
                ));
    }

    /**
     * Aggregates log counts by host
     */
    public Map<String, Long> aggregateByHost(List<ParsedLogEvent> events) {
        return events.stream()
                .filter(event -> event.getHost() != null && !event.getHost().isBlank())
                .collect(Collectors.groupingBy(
                        ParsedLogEvent::getHost,
                        Collectors.counting()
                ));
    }

    /**
     * Calculates statistical summary for error counts over time
     */
    public StatisticalSummary calculateStatistics(List<ParsedLogEvent> events, long bucketSizeMinutes) {
        Map<Instant, Long> timeBuckets = aggregateByTimeBucket(events, bucketSizeMinutes);

        if (timeBuckets.isEmpty()) {
            return new StatisticalSummary(0, 0, 0, 0, 0);
        }

        List<Long> counts = new ArrayList<>(timeBuckets.values());
        Collections.sort(counts);

        long min = counts.get(0);
        long max = counts.get(counts.size() - 1);
        double mean = counts.stream().mapToLong(Long::longValue).average().orElse(0);

        double median;
        int size = counts.size();
        if (size % 2 == 0) {
            median = (counts.get(size / 2 - 1) + counts.get(size / 2)) / 2.0;
        } else {
            median = counts.get(size / 2);
        }

        double variance = counts.stream()
                .mapToDouble(count -> Math.pow(count - mean, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);

        return new StatisticalSummary(min, max, mean, median, stdDev);
    }

    /**
     * Groups events by trace ID for distributed tracing analysis
     */
    public Map<String, List<ParsedLogEvent>> groupByTraceId(List<ParsedLogEvent> events) {
        return events.stream()
                .filter(event -> event.getTraceId() != null && !event.getTraceId().isBlank())
                .collect(Collectors.groupingBy(ParsedLogEvent::getTraceId));
    }

    /**
     * Rounds a timestamp to the nearest time bucket
     */
    private Instant roundToTimeBucket(Instant timestamp, long bucketSizeMinutes) {
        long epochMinutes = timestamp.getEpochSecond() / 60;
        long bucketMinutes = (epochMinutes / bucketSizeMinutes) * bucketSizeMinutes;
        return Instant.ofEpochSecond(bucketMinutes * 60);
    }

    public static class StatisticalSummary {
        private final long min;
        private final long max;
        private final double mean;
        private final double median;
        private final double stdDev;

        public StatisticalSummary(long min, long max, double mean, double median, double stdDev) {
            this.min = min;
            this.max = max;
            this.mean = mean;
            this.median = median;
            this.stdDev = stdDev;
        }

        public long getMin() { return min; }
        public long getMax() { return max; }
        public double getMean() { return mean; }
        public double getMedian() { return median; }
        public double getStdDev() { return stdDev; }
    }
}
