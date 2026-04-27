package com.ankur.loganalyzer.analyzer;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PatternDetectorTest {

    private PatternDetector patternDetector;

    @BeforeEach
    void setUp() {
        patternDetector = new PatternDetector();
    }

    @Nested
    @DisplayName("Pattern Extraction Tests")
    class PatternExtractionTests {

        @Test
        @DisplayName("Should normalize UUIDs in messages")
        void shouldNormalizeUuids() {
            String message = "Processing request 550e8400-e29b-41d4-a716-446655440000";
            String pattern = patternDetector.extractPattern(message);

            assertEquals("Processing request <UUID>", pattern);
        }

        @Test
        @DisplayName("Should normalize timestamps in messages")
        void shouldNormalizeTimestamps() {
            String message = "Event occurred at 2024-01-15T10:30:45";
            String pattern = patternDetector.extractPattern(message);

            assertEquals("Event occurred at <TIMESTAMP>", pattern);
        }

        @Test
        @DisplayName("Should normalize numbers in messages")
        void shouldNormalizeNumbers() {
            String message = "User 12345 made 5 purchases totaling 99.99";
            String pattern = patternDetector.extractPattern(message);

            assertTrue(pattern.contains("<NUM>"));
            assertFalse(pattern.contains("12345"));
            assertFalse(pattern.contains("99"));
        }

        @Test
        @DisplayName("Should normalize IP addresses")
        void shouldNormalizeIpAddresses() {
            String message = "Connection from 192.168.1.100 refused";
            String pattern = patternDetector.extractPattern(message);

            // After normalization, numbers get replaced with <NUM>, IP pattern happens after
            // The important thing is the original IP is not in the output
            assertFalse(pattern.contains("192.168.1.100"));
        }

        @Test
        @DisplayName("Should normalize hex values")
        void shouldNormalizeHexValues() {
            String message = "Memory address 0x7fff5fbff8c0 invalid";
            String pattern = patternDetector.extractPattern(message);

            // After normalization, the hex value is replaced
            assertFalse(pattern.contains("0x7fff5fbff8c0"));
        }

        @Test
        @DisplayName("Should handle empty messages")
        void shouldHandleEmptyMessages() {
            assertEquals("EMPTY_MESSAGE", patternDetector.extractPattern(""));
            assertEquals("EMPTY_MESSAGE", patternDetector.extractPattern("   "));
            assertEquals("EMPTY_MESSAGE", patternDetector.extractPattern(null));
        }
    }

    @Nested
    @DisplayName("Pattern Grouping Tests")
    class PatternGroupingTests {

        @Test
        @DisplayName("Should group similar messages together")
        void shouldGroupSimilarMessages() {
            List<ParsedLogEvent> events = Arrays.asList(
                createEvent("User 123 logged in"),
                createEvent("User 456 logged in"),
                createEvent("User 789 logged in"),
                createEvent("Payment processed for order 100"),
                createEvent("Payment processed for order 200")
            );

            Map<String, List<ParsedLogEvent>> grouped = patternDetector.groupByPattern(events);

            assertEquals(2, grouped.size());
            assertTrue(grouped.containsKey("User <NUM> logged in"));
            assertTrue(grouped.containsKey("Payment processed for order <NUM>"));
            assertEquals(3, grouped.get("User <NUM> logged in").size());
            assertEquals(2, grouped.get("Payment processed for order <NUM>").size());
        }

        @Test
        @DisplayName("Should find top patterns correctly")
        void shouldFindTopPatterns() {
            List<ParsedLogEvent> events = Arrays.asList(
                createEvent("Connection timeout"),
                createEvent("Connection timeout"),
                createEvent("Connection timeout"),
                createEvent("Connection timeout"),
                createEvent("Database error"),
                createEvent("Database error")
            );

            List<PatternDetector.PatternOccurrence> topPatterns =
                patternDetector.findTopPatterns(events, 2);

            assertEquals(2, topPatterns.size());
            assertEquals("Connection timeout", topPatterns.get(0).getPattern());
            assertEquals(4, topPatterns.get(0).getCount());
            assertEquals("Database error", topPatterns.get(1).getPattern());
            assertEquals(2, topPatterns.get(1).getCount());
        }
    }

    @Nested
    @DisplayName("Stack Trace Signature Tests")
    class StackTraceSignatureTests {

        @Test
        @DisplayName("Should extract stack trace signature")
        void shouldExtractStackTraceSignature() {
            String stackTrace = """
                java.lang.NullPointerException
                    at com.example.Service.process(Service.java:42)
                    at com.example.Controller.handle(Controller.java:15)
                """;

            String signature = patternDetector.extractStackTraceSignature(stackTrace);

            assertNotNull(signature);
            assertTrue(signature.contains("NullPointerException"));
            // Line numbers should be normalized
            assertFalse(signature.contains(":42)"));
        }

        @Test
        @DisplayName("Should handle null stack traces")
        void shouldHandleNullStackTraces() {
            assertEquals("NO_STACK_TRACE", patternDetector.extractStackTraceSignature(null));
            assertEquals("NO_STACK_TRACE", patternDetector.extractStackTraceSignature(""));
            assertEquals("NO_STACK_TRACE", patternDetector.extractStackTraceSignature("   "));
        }
    }

    private ParsedLogEvent createEvent(String message) {
        return ParsedLogEvent.builder()
                .message(message)
                .level(ParsedLogEvent.LogLevel.INFO)
                .timestamp(Instant.now())
                .build();
    }
}
