package com.ankur.loganalyzer.analyzer;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Detects anomalies in log patterns using statistical methods
 */
@Component
public class AnomalyDetector {

    private static final double Z_SCORE_THRESHOLD = 2.5; // Standard deviations from mean
    private static final double SPIKE_THRESHOLD = 2.0; // Multiplier for spike detection

    /**
     * Detects anomalies using Z-score method
     * Returns events that are statistical outliers
     */
    public List<Anomaly> detectAnomaliesWithZScore(Map<Instant, Long> timeSeries) {
        if (timeSeries.size() < 3) {
            return Collections.emptyList();
        }

        double mean = calculateMean(timeSeries.values());
        double stdDev = calculateStdDev(timeSeries.values(), mean);

        if (stdDev == 0) {
            return Collections.emptyList();
        }

        List<Anomaly> anomalies = new ArrayList<>();

        for (Map.Entry<Instant, Long> entry : timeSeries.entrySet()) {
            double zScore = (entry.getValue() - mean) / stdDev;

            if (Math.abs(zScore) > Z_SCORE_THRESHOLD) {
                anomalies.add(new Anomaly(
                        entry.getKey(),
                        entry.getValue(),
                        mean,
                        zScore,
                        zScore > 0 ? AnomalyType.SPIKE : AnomalyType.DROP
                ));
            }
        }

        return anomalies;
    }

    /**
     * Detects sudden spikes by comparing each value to a moving average
     */
    public List<Anomaly> detectSpikes(Map<Instant, Long> timeSeries, int windowSize) {
        if (timeSeries.size() < windowSize + 1) {
            return Collections.emptyList();
        }

        List<Anomaly> spikes = new ArrayList<>();
        List<Map.Entry<Instant, Long>> entries = new ArrayList<>(timeSeries.entrySet());

        for (int i = windowSize; i < entries.size(); i++) {
            double movingAvg = calculateMovingAverage(entries, i - windowSize, i);
            Map.Entry<Instant, Long> current = entries.get(i);

            if (current.getValue() > movingAvg * SPIKE_THRESHOLD) {
                double zScore = (current.getValue() - movingAvg) / Math.max(1, Math.sqrt(movingAvg));
                spikes.add(new Anomaly(
                        current.getKey(),
                        current.getValue(),
                        movingAvg,
                        zScore,
                        AnomalyType.SPIKE
                ));
            }
        }

        return spikes;
    }

    /**
     * Detects sustained increases in error rate (not just spikes)
     */
    public List<Anomaly> detectSustainedIncrease(Map<Instant, Long> timeSeries, int windowSize) {
        if (timeSeries.size() < windowSize * 2) {
            return Collections.emptyList();
        }

        List<Anomaly> anomalies = new ArrayList<>();
        List<Map.Entry<Instant, Long>> entries = new ArrayList<>(timeSeries.entrySet());

        for (int i = windowSize; i < entries.size() - windowSize; i++) {
            double previousAvg = calculateMovingAverage(entries, i - windowSize, i);
            double currentAvg = calculateMovingAverage(entries, i, i + windowSize);

            if (currentAvg > previousAvg * 1.5 && previousAvg > 0) {
                Map.Entry<Instant, Long> point = entries.get(i + windowSize / 2);
                double change = ((currentAvg - previousAvg) / previousAvg) * 100;

                anomalies.add(new Anomaly(
                        point.getKey(),
                        point.getValue(),
                        previousAvg,
                        change,
                        AnomalyType.SUSTAINED_INCREASE
                ));
            }
        }

        return anomalies;
    }

    /**
     * Detects unusual patterns in service-level errors
     */
    public List<ServiceAnomaly> detectServiceAnomalies(Map<String, Long> currentCounts,
                                                        Map<String, Long> baselineCounts) {
        List<ServiceAnomaly> anomalies = new ArrayList<>();

        for (Map.Entry<String, Long> entry : currentCounts.entrySet()) {
            String service = entry.getKey();
            long currentCount = entry.getValue();
            long baselineCount = baselineCounts.getOrDefault(service, 0L);

            if (baselineCount > 0) {
                double changePercent = ((double) (currentCount - baselineCount) / baselineCount) * 100;

                if (Math.abs(changePercent) > 50) { // 50% change threshold
                    anomalies.add(new ServiceAnomaly(
                            service,
                            currentCount,
                            baselineCount,
                            changePercent,
                            changePercent > 0 ? AnomalyType.SPIKE : AnomalyType.DROP
                    ));
                }
            } else if (currentCount > 10) {
                // New service with significant errors
                anomalies.add(new ServiceAnomaly(
                        service,
                        currentCount,
                        0,
                        100.0,
                        AnomalyType.NEW_ERROR_SOURCE
                ));
            }
        }

        return anomalies;
    }

    private double calculateMean(Collection<Long> values) {
        return values.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private double calculateStdDev(Collection<Long> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    private double calculateMovingAverage(List<Map.Entry<Instant, Long>> entries, int start, int end) {
        return entries.subList(start, end).stream()
                .mapToLong(Map.Entry::getValue)
                .average()
                .orElse(0);
    }

    public enum AnomalyType {
        SPIKE,
        DROP,
        SUSTAINED_INCREASE,
        SUSTAINED_DECREASE,
        NEW_ERROR_SOURCE
    }

    public static class Anomaly {
        private final Instant timestamp;
        private final long actualValue;
        private final double expectedValue;
        private final double zScore;
        private final AnomalyType type;

        public Anomaly(Instant timestamp, long actualValue, double expectedValue, double zScore, AnomalyType type) {
            this.timestamp = timestamp;
            this.actualValue = actualValue;
            this.expectedValue = expectedValue;
            this.zScore = zScore;
            this.type = type;
        }

        public Instant getTimestamp() { return timestamp; }
        public long getActualValue() { return actualValue; }
        public double getExpectedValue() { return expectedValue; }
        public double getZScore() { return zScore; }
        public AnomalyType getType() { return type; }
    }

    public static class ServiceAnomaly {
        private final String serviceName;
        private final long currentCount;
        private final long baselineCount;
        private final double changePercent;
        private final AnomalyType type;

        public ServiceAnomaly(String serviceName, long currentCount, long baselineCount, double changePercent, AnomalyType type) {
            this.serviceName = serviceName;
            this.currentCount = currentCount;
            this.baselineCount = baselineCount;
            this.changePercent = changePercent;
            this.type = type;
        }

        public String getServiceName() { return serviceName; }
        public long getCurrentCount() { return currentCount; }
        public long getBaselineCount() { return baselineCount; }
        public double getChangePercent() { return changePercent; }
        public AnomalyType getType() { return type; }
    }
}
