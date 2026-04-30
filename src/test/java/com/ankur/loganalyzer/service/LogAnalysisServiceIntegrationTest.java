package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("LogAnalysisService Integration Tests")
class LogAnalysisServiceIntegrationTest {

    @Autowired
    private LogAnalysisService logAnalysisService;

    @Autowired
    private ParsedLogEventRepository parsedLogEventRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @BeforeEach
    void setUp() {
        analysisResultRepository.deleteAll();
        parsedLogEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should generate summary for current log window")
    void generateSummarySuccess() {
        Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(1, ChronoUnit.MINUTES);
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-1", ParsedLogEvent.LogLevel.ERROR, "Error occurred"));
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-1", ParsedLogEvent.LogLevel.WARN, "Warning occurred"));
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-1", ParsedLogEvent.LogLevel.INFO, "Info message"));

        var result = logAnalysisService.generateSummary(start, end);

        assertNotNull(result);
        assertEquals(3, result.totalLogs());
        assertEquals(1, result.totalErrors());
        assertEquals(1, result.totalWarnings());
    }

    @Test
    @DisplayName("Should detect anomaly response for error logs")
    void detectAnomalies() {
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(1, ChronoUnit.MINUTES);
        for (int i = 0; i < 20; i++) {
            parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-1", ParsedLogEvent.LogLevel.ERROR, "Anomaly " + i));
        }

        var anomalies = logAnalysisService.detectAnomalies(start, end, 5);

        assertNotNull(anomalies);
        assertNotNull(anomalies.statistics());
        assertNotNull(anomalies.timeSeriesAnomalies());
        assertNotNull(anomalies.serviceAnomalies());
    }

    @Test
    @DisplayName("Should calculate aggregation distribution")
    void calculateAggregationDistribution() {
        Instant start = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant end = Instant.now().plus(1, ChronoUnit.MINUTES);
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-1", ParsedLogEvent.LogLevel.ERROR, "DB Error"));
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-1", ParsedLogEvent.LogLevel.WARN, "Warning"));
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("service-2", ParsedLogEvent.LogLevel.FATAL, "Fatal"));

        var distribution = logAnalysisService.getAggregations(start, end, 5);

        assertNotNull(distribution);
        assertEquals(3, distribution.totalLogs());
        assertTrue(distribution.byLevel().containsKey("ERROR"));
        assertTrue(distribution.byService().containsKey("service-1"));
    }
}
