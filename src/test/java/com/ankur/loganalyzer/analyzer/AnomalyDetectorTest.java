package com.ankur.loganalyzer.analyzer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class AnomalyDetectorTest {

    private AnomalyDetector anomalyDetector;

    @BeforeEach
    void setUp() {
        anomalyDetector = new AnomalyDetector();
    }

    @Nested
    @DisplayName("Z-Score Anomaly Detection Tests")
    class ZScoreAnomalyTests {

        @Test
        @DisplayName("Should detect spike anomaly with Z-score")
        void shouldDetectSpikeAnomalyWithZScore() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            // Normal values around 10
            for (int i = 0; i < 10; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 10L + (i % 3));
            }
            // Spike value
            timeSeries.put(base.plus(10, ChronoUnit.MINUTES), 100L);

            List<AnomalyDetector.Anomaly> anomalies =
                anomalyDetector.detectAnomaliesWithZScore(timeSeries);

            assertFalse(anomalies.isEmpty());
            assertTrue(anomalies.stream().anyMatch(a -> a.getType() == AnomalyDetector.AnomalyType.SPIKE));
        }

        @Test
        @DisplayName("Should detect drop anomaly with Z-score")
        void shouldDetectDropAnomalyWithZScore() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            // Normal values around 100
            for (int i = 0; i < 10; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 100L);
            }
            // Drop value
            timeSeries.put(base.plus(10, ChronoUnit.MINUTES), 1L);

            List<AnomalyDetector.Anomaly> anomalies =
                anomalyDetector.detectAnomaliesWithZScore(timeSeries);

            assertFalse(anomalies.isEmpty());
            assertTrue(anomalies.stream().anyMatch(a -> a.getType() == AnomalyDetector.AnomalyType.DROP));
        }

        @Test
        @DisplayName("Should return empty list for stable time series")
        void shouldReturnEmptyForStableTimeSeries() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            // All same values - no variance
            for (int i = 0; i < 10; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 10L);
            }

            List<AnomalyDetector.Anomaly> anomalies =
                anomalyDetector.detectAnomaliesWithZScore(timeSeries);

            assertTrue(anomalies.isEmpty());
        }

        @Test
        @DisplayName("Should handle small dataset")
        void shouldHandleSmallDataset() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            timeSeries.put(base, 10L);
            timeSeries.put(base.plus(1, ChronoUnit.MINUTES), 100L);

            // Should not crash, but may return empty for insufficient data
            List<AnomalyDetector.Anomaly> anomalies =
                anomalyDetector.detectAnomaliesWithZScore(timeSeries);

            assertNotNull(anomalies);
        }
    }

    @Nested
    @DisplayName("Spike Detection Tests")
    class SpikeDetectionTests {

        @Test
        @DisplayName("Should detect spikes using moving average")
        void shouldDetectSpikesUsingMovingAverage() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            // Normal values
            for (int i = 0; i < 10; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 10L);
            }
            // Spike (more than 2x the moving average)
            timeSeries.put(base.plus(10, ChronoUnit.MINUTES), 50L);

            List<AnomalyDetector.Anomaly> spikes =
                anomalyDetector.detectSpikes(timeSeries, 5);

            assertFalse(spikes.isEmpty());
            assertEquals(AnomalyDetector.AnomalyType.SPIKE, spikes.get(0).getType());
        }

        @Test
        @DisplayName("Should not detect normal variation as spike")
        void shouldNotDetectNormalVariationAsSpike() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            // Values with normal variation (10-15)
            for (int i = 0; i < 15; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 10L + (i % 5));
            }

            List<AnomalyDetector.Anomaly> spikes =
                anomalyDetector.detectSpikes(timeSeries, 5);

            assertTrue(spikes.isEmpty());
        }
    }

    @Nested
    @DisplayName("Service Anomaly Detection Tests")
    class ServiceAnomalyTests {

        @Test
        @DisplayName("Should detect service with increased errors")
        void shouldDetectServiceWithIncreasedErrors() {
            Map<String, Long> currentCounts = new LinkedHashMap<>();
            currentCounts.put("user-service", 100L);
            currentCounts.put("order-service", 50L);

            Map<String, Long> baselineCounts = new LinkedHashMap<>();
            baselineCounts.put("user-service", 30L);  // 233% increase
            baselineCounts.put("order-service", 45L); // 11% increase

            List<AnomalyDetector.ServiceAnomaly> anomalies =
                anomalyDetector.detectServiceAnomalies(currentCounts, baselineCounts);

            assertEquals(1, anomalies.size());
            assertEquals("user-service", anomalies.get(0).getServiceName());
            assertEquals(AnomalyDetector.AnomalyType.SPIKE, anomalies.get(0).getType());
        }

        @Test
        @DisplayName("Should detect service with decreased errors")
        void shouldDetectServiceWithDecreasedErrors() {
            Map<String, Long> currentCounts = new LinkedHashMap<>();
            currentCounts.put("payment-service", 10L);

            Map<String, Long> baselineCounts = new LinkedHashMap<>();
            baselineCounts.put("payment-service", 100L); // 90% decrease

            List<AnomalyDetector.ServiceAnomaly> anomalies =
                anomalyDetector.detectServiceAnomalies(currentCounts, baselineCounts);

            assertEquals(1, anomalies.size());
            assertEquals(AnomalyDetector.AnomalyType.DROP, anomalies.get(0).getType());
            assertTrue(anomalies.get(0).getChangePercent() < 0);
        }

        @Test
        @DisplayName("Should detect new error source")
        void shouldDetectNewErrorSource() {
            Map<String, Long> currentCounts = new LinkedHashMap<>();
            currentCounts.put("new-service", 20L); // New service with significant errors

            Map<String, Long> baselineCounts = new LinkedHashMap<>();
            // new-service not in baseline

            List<AnomalyDetector.ServiceAnomaly> anomalies =
                anomalyDetector.detectServiceAnomalies(currentCounts, baselineCounts);

            assertEquals(1, anomalies.size());
            assertEquals(AnomalyDetector.AnomalyType.NEW_ERROR_SOURCE, anomalies.get(0).getType());
        }

        @Test
        @DisplayName("Should ignore new service with few errors")
        void shouldIgnoreNewServiceWithFewErrors() {
            Map<String, Long> currentCounts = new LinkedHashMap<>();
            currentCounts.put("new-service", 5L); // Few errors - below threshold

            Map<String, Long> baselineCounts = new LinkedHashMap<>();

            List<AnomalyDetector.ServiceAnomaly> anomalies =
                anomalyDetector.detectServiceAnomalies(currentCounts, baselineCounts);

            assertTrue(anomalies.isEmpty());
        }
    }

    @Nested
    @DisplayName("Sustained Increase Detection Tests")
    class SustainedIncreaseTests {

        @Test
        @DisplayName("Should detect sustained increase pattern")
        void shouldDetectSustainedIncrease() {
            Map<Instant, Long> timeSeries = new TreeMap<>();
            Instant base = Instant.now();

            // Low period
            for (int i = 0; i < 10; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 10L);
            }
            // Sustained high period
            for (int i = 10; i < 20; i++) {
                timeSeries.put(base.plus(i, ChronoUnit.MINUTES), 50L);
            }

            List<AnomalyDetector.Anomaly> anomalies =
                anomalyDetector.detectSustainedIncrease(timeSeries, 5);

            assertFalse(anomalies.isEmpty());
            assertTrue(anomalies.stream()
                .anyMatch(a -> a.getType() == AnomalyDetector.AnomalyType.SUSTAINED_INCREASE));
        }
    }
}
