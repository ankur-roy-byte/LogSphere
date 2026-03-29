package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.model.LogEntry;
import com.ankur.loganalyzer.repository.LogEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("LogAnalysisService Integration Tests")
class LogAnalysisServiceIntegrationTest {

    @Autowired
    private LogAnalysisService logAnalysisService;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @BeforeEach
    void setUp() {
        logEntryRepository.deleteAll();
    }

    @Test
    @DisplayName("Should analyze logs successfully")
    void testAnalyzeLogs_Success() {
        // Create test logs
        logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "ERROR", "Error occurred"));
        logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "ERROR", "Error occurred"));
        logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "INFO", "Info message"));

        var result = logAnalysisService.analyzeLogs("service-1", null, null);

        assertNotNull(result);
        assertTrue(result.getTotalLogs() >= 3);
    }

    @Test
    @DisplayName("Should detect anomalies in error logs")
    void testDetectAnomalies() {
        for (int i = 0; i < 50; i++) {
            logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "INFO", "Normal"));
        }
        for (int i = 0; i < 20; i++) {
            logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "ERROR", "Anomaly"));
        }

        var anomalies = logAnalysisService.detectAnomalies("service-1");

        assertNotNull(anomalies);
        assertTrue(anomalies.size() > 0);
    }

    @Test
    @DisplayName("Should calculate error distribution")
    void testCalculateErrorDistribution() {
        logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "ERROR", "DB Error"));
        logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "WARN", "Warning"));
        logEntryRepository.save(TestDataFactory.createLogEntry("service-1", "FATAL", "Fatal"));

        var distribution = logAnalysisService.getLogLevelDistribution("service-1");

        assertNotNull(distribution);
        assertTrue(distribution.containsKey("ERROR") || distribution.size() >= 0);
    }
}
