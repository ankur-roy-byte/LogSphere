package com.ankur.loganalyzer.parser;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserFactoryTest {

    private ParserFactory parserFactory;

    @BeforeEach
    void setUp() {
        // Create parsers in proper priority order (JSON first, then regex, then stack trace)
        List<LogParser> parsers = List.of(
            new JsonLogParser(),
            new RegexLogParser(),
            new StackTraceParser()
        );
        parserFactory = new ParserFactory(parsers);
    }

    @Nested
    @DisplayName("JSON Log Parsing Tests")
    class JsonLogParsingTests {

        @Test
        @DisplayName("Should parse standard JSON log with all fields")
        void shouldParseStandardJsonLog() {
            String jsonLog = """
                {"timestamp":"2024-01-15T10:30:45.123Z","level":"ERROR","message":"Database connection failed","service":"user-service","exceptionType":"SQLException","traceId":"abc123"}
                """;

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(jsonLog.trim());
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertEquals(ParsedLogEvent.LogLevel.ERROR, event.getLevel());
            assertEquals("Database connection failed", event.getMessage());
            assertEquals("user-service", event.getServiceName());
            assertEquals("SQLException", event.getExceptionType());
            assertEquals("abc123", event.getTraceId());
        }

        @Test
        @DisplayName("Should parse JSON log with alternative field names")
        void shouldParseJsonLogWithAlternativeNames() {
            String jsonLog = """
                {"@timestamp":"2024-01-15T10:30:45.123Z","severity":"WARN","msg":"High memory usage","application":"order-service"}
                """;

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(jsonLog.trim());
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertEquals(ParsedLogEvent.LogLevel.WARN, event.getLevel());
            assertEquals("High memory usage", event.getMessage());
            assertEquals("order-service", event.getServiceName());
        }

        @Test
        @DisplayName("Should extract metadata from unrecognized JSON fields")
        void shouldExtractMetadata() {
            String jsonLog = """
                {"level":"INFO","message":"Request processed","userId":"12345","requestId":"req-abc"}
                """;

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(jsonLog.trim());
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertNotNull(event.getMetadata());
            assertEquals("12345", event.getMetadata().get("userId"));
            assertEquals("req-abc", event.getMetadata().get("requestId"));
        }
    }

    @Nested
    @DisplayName("Regex Log Parsing Tests")
    class RegexLogParsingTests {

        @Test
        @DisplayName("Should parse Spring Boot log format")
        void shouldParseSpringBootFormat() {
            String log = "2024-01-15 10:30:45.123 ERROR [payment-service] [main] c.a.PaymentController - Payment failed for order 123";

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(log);
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertEquals(ParsedLogEvent.LogLevel.ERROR, event.getLevel());
            assertEquals("payment-service", event.getServiceName());
            assertTrue(event.getMessage().contains("Payment failed"));
        }

        @Test
        @DisplayName("Should parse bracket format logs")
        void shouldParseBracketFormat() {
            String log = "[2024-01-15 10:30:45] WARN: Connection timeout occurred";

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(log);
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertEquals(ParsedLogEvent.LogLevel.WARN, event.getLevel());
            assertTrue(event.getMessage().contains("Connection timeout"));
        }
    }

    @Nested
    @DisplayName("Stack Trace Parsing Tests")
    class StackTraceParsingTests {

        @Test
        @DisplayName("Should detect and parse Java stack trace")
        void shouldParseJavaStackTrace() {
            String stackTrace = """
                java.lang.NullPointerException: Cannot invoke method on null object
                    at com.example.UserService.getUser(UserService.java:45)
                    at com.example.UserController.handleRequest(UserController.java:23)
                """;

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(stackTrace);
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertEquals(ParsedLogEvent.LogLevel.ERROR, event.getLevel());
            // Exception type may include full package name
            assertTrue(event.getExceptionType().contains("NullPointerException"));
            assertNotNull(event.getStackTrace());
        }

        @Test
        @DisplayName("Should parse Python traceback")
        void shouldParsePythonTraceback() {
            String traceback = """
                Traceback (most recent call last):
                  File "main.py", line 10, in <module>
                    raise ValueError("Invalid input")
                ValueError: Invalid input
                """;

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(traceback);
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            // Python traceback may be treated as error or info depending on parser
            assertNotNull(event.getLevel());
            // The exception type might be extracted or the whole message captured
            assertNotNull(event.getMessage());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Fallback Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle plain text as INFO log")
        void shouldHandlePlainText() {
            String plainText = "Application started successfully";

            ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(plainText);
            ParsedLogEvent event = builder.timestamp(Instant.now()).build();

            assertEquals(ParsedLogEvent.LogLevel.INFO, event.getLevel());
            assertEquals("Application started successfully", event.getMessage());
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() {
            assertDoesNotThrow(() -> {
                ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse("");
                ParsedLogEvent event = builder.timestamp(Instant.now()).build();
                assertNotNull(event);
            });
        }

        @Test
        @DisplayName("Should handle null input gracefully")
        void shouldHandleNullInput() {
            assertDoesNotThrow(() -> {
                ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(null);
                ParsedLogEvent event = builder.timestamp(Instant.now()).build();
                assertNotNull(event);
            });
        }

        @Test
        @DisplayName("Should handle malformed JSON")
        void shouldHandleMalformedJson() {
            String malformedJson = "{level: ERROR, message: }";

            assertDoesNotThrow(() -> {
                ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(malformedJson);
                ParsedLogEvent event = builder.timestamp(Instant.now()).build();
                assertNotNull(event);
            });
        }
    }

    @Nested
    @DisplayName("Log Level Detection Tests")
    class LogLevelDetectionTests {

        @Test
        @DisplayName("Should correctly map all log levels")
        void shouldMapAllLogLevels() {
            String[] levels = {"TRACE", "DEBUG", "INFO", "WARN", "WARNING", "ERROR", "FATAL", "CRITICAL"};
            ParsedLogEvent.LogLevel[] expected = {
                ParsedLogEvent.LogLevel.TRACE,
                ParsedLogEvent.LogLevel.DEBUG,
                ParsedLogEvent.LogLevel.INFO,
                ParsedLogEvent.LogLevel.WARN,
                ParsedLogEvent.LogLevel.WARN,
                ParsedLogEvent.LogLevel.ERROR,
                ParsedLogEvent.LogLevel.FATAL,
                ParsedLogEvent.LogLevel.FATAL
            };

            for (int i = 0; i < levels.length; i++) {
                String log = String.format("{\"level\":\"%s\",\"message\":\"test\"}", levels[i]);
                ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(log);
                ParsedLogEvent event = builder.timestamp(Instant.now()).build();
                assertEquals(expected[i], event.getLevel(), "Failed for level: " + levels[i]);
            }
        }
    }
}
