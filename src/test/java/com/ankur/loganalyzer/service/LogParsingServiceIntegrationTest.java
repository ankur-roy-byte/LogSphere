package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for log ingestion, parsing, and storage.
 */
@SpringBootTest
@ActiveProfiles("test")
class LogParsingServiceIntegrationTest {

    @Autowired
    private LogIngestionService logIngestionService;

    @Autowired
    private RawLogEventRepository rawLogEventRepository;

    @Autowired
    private ParsedLogEventRepository parsedLogEventRepository;

    @Autowired
    private LogSourceRepository logSourceRepository;

    @BeforeEach
    void setUp() {
        parsedLogEventRepository.deleteAll();
        rawLogEventRepository.deleteAll();
        logSourceRepository.deleteAll();
    }

    @Test
    void parseTextLog() {
        var result = logIngestionService.ingestFromUpload("[2024-01-15 10:30:45] INFO - User login successful", "auth-service");

        assertEquals(1, result.totalLines());
        assertEquals(1, result.parsedSuccessfully());
        ParsedLogEvent saved = parsedLogEventRepository.findAll().get(0);
        assertEquals(ParsedLogEvent.LogLevel.INFO, saved.getLevel());
        assertTrue(saved.getMessage().contains("User login successful"));
    }

    @Test
    void parseJsonLog() {
        String jsonContent = "{\"timestamp\":\"2024-01-15T10:30:45Z\",\"level\":\"ERROR\",\"message\":\"Database connection failed\",\"service\":\"db-service\"}";

        var result = logIngestionService.ingestFromUpload(jsonContent, "db-service");

        assertEquals(1, result.parsedSuccessfully());
        ParsedLogEvent saved = parsedLogEventRepository.findAll().get(0);
        assertEquals(ParsedLogEvent.LogLevel.ERROR, saved.getLevel());
        assertEquals("db-service", saved.getServiceName());
        assertEquals("Database connection failed", saved.getMessage());
    }

    @Test
    void parseStackTraceAsSingleEvent() {
        String stackTrace = """
                Exception in thread "main" java.lang.NullPointerException
                    at com.example.Service.process(Service.java:42)
                    at com.example.Main.main(Main.java:10)
                """;

        var result = logIngestionService.ingestFromUpload(stackTrace, "api-service");

        assertEquals(1, result.totalLines());
        assertEquals(1, result.parsedSuccessfully());
        ParsedLogEvent saved = parsedLogEventRepository.findAll().get(0);
        assertNotNull(saved.getId());
        assertTrue(saved.getMessage().contains("NullPointerException"));
    }

    @Test
    void parseBulkOperation() {
        String logs = """
                {"timestamp":"2024-01-15T10:30:45Z","level":"INFO","message":"Log message 1","service":"test-service"}
                {"timestamp":"2024-01-15T10:30:46Z","level":"WARN","message":"Log message 2","service":"test-service"}
                {"timestamp":"2024-01-15T10:30:47Z","level":"ERROR","message":"Log message 3","service":"test-service"}
                """;

        var result = logIngestionService.ingestFromUpload(logs, "test-service");

        assertEquals(3, result.totalLines());
        assertEquals(3, result.parsedSuccessfully());
        assertEquals(3, rawLogEventRepository.count());
        assertEquals(3, parsedLogEventRepository.count());
    }

    @Test
    void parseLogWithTraceId() {
        String logWithTrace = "{\"timestamp\":\"2024-01-15T10:30:45Z\",\"level\":\"INFO\",\"message\":\"Request processed\",\"service\":\"api-service\",\"traceId\":\"550e8400-e29b-41d4-a716-446655440000\"}";

        logIngestionService.ingestFromUpload(logWithTrace, "api-service");

        ParsedLogEvent saved = parsedLogEventRepository.findAll().get(0);
        assertEquals("550e8400-e29b-41d4-a716-446655440000", saved.getTraceId());
    }
}
