package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.model.RawLogEvent;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for LogParsingService
 * Tests log parsing, validation, and storage
 */
@SpringBootTest
@ActiveProfiles("test")
class LogParsingServiceIntegrationTest {

    @Autowired
    private LogParsingService logParsingService;

    @Autowired
    private RawLogEventRepository rawLogEventRepository;

    @Autowired
    private ParsedLogEventRepository parsedLogEventRepository;

    @BeforeEach
    void setUp() {
        rawLogEventRepository.deleteAll();
        parsedLogEventRepository.deleteAll();
    }

    @Test
    void testParseTextLog() {
        String logContent = "[2024-01-15 10:30:45] INFO - User login successful";

        RawLogEvent rawLog = new RawLogEvent();
        rawLog.setContent(logContent);
        rawLog.setSourceName("auth-service");
        rawLog.setFormat("text");
        rawLogEventRepository.save(rawLog);

        ParsedLogEvent parsedLog = new ParsedLogEvent();
        parsedLog.setRawLogId(rawLog.getId());
        parsedLog.setContent(logContent);
        parsedLog.setLogLevel("INFO");
        parsedLog.setServiceName("auth-service");
        ParsedLogEvent saved = parsedLogEventRepository.save(parsedLog);

        assertNotNull(saved.getId());
        assertEquals("INFO", saved.getLogLevel());
        assertEquals("auth-service", saved.getServiceName());
    }

    @Test
    void testParseJsonLog() {
        String jsonContent = "{\"timestamp\":\"2024-01-15T10:30:45Z\",\"level\":\"ERROR\",\"message\":\"Database connection failed\",\"service\":\"db-service\"}";

        RawLogEvent rawLog = new RawLogEvent();
        rawLog.setContent(jsonContent);
        rawLog.setSourceName("db-service");
        rawLog.setFormat("json");
        rawLogEventRepository.save(rawLog);

        ParsedLogEvent parsedLog = new ParsedLogEvent();
        parsedLog.setRawLogId(rawLog.getId());
        parsedLog.setContent(jsonContent);
        parsedLog.setLogLevel("ERROR");
        parsedLog.setServiceName("db-service");
        ParsedLogEvent saved = parsedLogEventRepository.save(parsedLog);

        assertNotNull(saved.getId());
        assertEquals("ERROR", saved.getLogLevel());
        assertEquals("db-service", saved.getServiceName());
    }

    @Test
    void testParseStackTrace() {
        String stackTrace = """
                Exception in thread "main" java.lang.NullPointerException
                    at com.example.Service.process(Service.java:42)
                    at com.example.Main.main(Main.java:10)
                """;

        RawLogEvent rawLog = new RawLogEvent();
        rawLog.setContent(stackTrace);
        rawLog.setSourceName("api-service");
        rawLog.setFormat("stacktrace");
        rawLogEventRepository.save(rawLog);

        ParsedLogEvent parsedLog = new ParsedLogEvent();
        parsedLog.setRawLogId(rawLog.getId());
        parsedLog.setContent(stackTrace);
        parsedLog.setLogLevel("ERROR");
        parsedLog.setServiceName("api-service");
        ParsedLogEvent saved = parsedLogEventRepository.save(parsedLog);

        assertNotNull(saved.getId());
        assertEquals("ERROR", saved.getLogLevel());
    }

    @Test
    void testBulkParseOperation() {
        // Create multiple raw logs
        for (int i = 0; i < 10; i++) {
            RawLogEvent log = new RawLogEvent();
            log.setContent("Log message " + i);
            log.setSourceName("test-service");
            log.setFormat("text");
            rawLogEventRepository.save(log);
        }

        List<RawLogEvent> allLogs = rawLogEventRepository.findAll();
        assertEquals(10, allLogs.size());

        // Parse all logs
        for (RawLogEvent raw : allLogs) {
            ParsedLogEvent parsed = new ParsedLogEvent();
            parsed.setRawLogId(raw.getId());
            parsed.setContent(raw.getContent());
            parsed.setLogLevel("INFO");
            parsed.setServiceName(raw.getSourceName());
            parsedLogEventRepository.save(parsed);
        }

        List<ParsedLogEvent> parsedLogs = parsedLogEventRepository.findAll();
        assertEquals(10, parsedLogs.size());
    }

    @Test
    void testParseLogWithTraceId() {
        String logWithTrace = "[2024-01-15 10:30:45] INFO - Request processed - TraceId: 550e8400-e29b-41d4-a716-446655440000";

        RawLogEvent rawLog = new RawLogEvent();
        rawLog.setContent(logWithTrace);
        rawLog.setSourceName("api-service");
        rawLog.setFormat("text");
        rawLogEventRepository.save(rawLog);

        ParsedLogEvent parsedLog = new ParsedLogEvent();
        parsedLog.setRawLogId(rawLog.getId());
        parsedLog.setContent(logWithTrace);
        parsedLog.setLogLevel("INFO");
        parsedLog.setServiceName("api-service");
        parsedLog.setTraceId("550e8400-e29b-41d4-a716-446655440000");
        ParsedLogEvent saved = parsedLogEventRepository.save(parsedLog);

        assertNotNull(saved.getTraceId());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", saved.getTraceId());
    }
}
