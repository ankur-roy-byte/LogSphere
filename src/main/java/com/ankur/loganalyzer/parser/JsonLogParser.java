package com.ankur.loganalyzer.parser;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@Order(1)
public class JsonLogParser implements LogParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String rawLog) {
        if (rawLog == null || rawLog.isBlank()) return false;
        String trimmed = rawLog.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    @Override
    public ParsedLogEvent.ParsedLogEventBuilder parse(String rawLog) {
        try {
            JsonNode root = objectMapper.readTree(rawLog);

            String level = extractField(root, "level", "severity", "log_level", "loglevel");
            String message = extractField(root, "message", "msg", "log", "text");
            String timestamp = extractField(root, "timestamp", "@timestamp", "time", "datetime");
            String service = extractField(root, "service", "serviceName", "service_name", "app", "application");
            String exception = extractField(root, "exception", "exceptionType", "exception_type", "error_type");
            String stackTrace = extractField(root, "stackTrace", "stack_trace", "stacktrace", "trace");
            String traceId = extractField(root, "traceId", "trace_id", "correlationId", "correlation_id");
            String host = extractField(root, "host", "hostname", "server");

            Map<String, String> metadata = new HashMap<>();
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode value = root.get(fieldName);
                if (value.isValueNode()) {
                    metadata.put(fieldName, value.asText());
                }
            }

            return ParsedLogEvent.builder()
                    .level(parseLevel(level))
                    .message(message != null ? message : rawLog)
                    .timestamp(parseTimestamp(timestamp))
                    .serviceName(service)
                    .exceptionType(exception)
                    .stackTrace(stackTrace)
                    .traceId(traceId)
                    .host(host)
                    .metadata(metadata);

        } catch (Exception e) {
            return ParsedLogEvent.builder()
                    .level(ParsedLogEvent.LogLevel.INFO)
                    .message(rawLog)
                    .timestamp(Instant.now());
        }
    }

    private String extractField(JsonNode root, String... possibleNames) {
        for (String name : possibleNames) {
            JsonNode node = root.get(name);
            if (node != null && !node.isNull()) {
                return node.asText();
            }
        }
        return null;
    }

    private ParsedLogEvent.LogLevel parseLevel(String level) {
        if (level == null) return ParsedLogEvent.LogLevel.INFO;
        return switch (level.toUpperCase()) {
            case "TRACE" -> ParsedLogEvent.LogLevel.TRACE;
            case "DEBUG" -> ParsedLogEvent.LogLevel.DEBUG;
            case "WARN", "WARNING" -> ParsedLogEvent.LogLevel.WARN;
            case "ERROR", "ERR", "SEVERE" -> ParsedLogEvent.LogLevel.ERROR;
            case "FATAL", "CRITICAL" -> ParsedLogEvent.LogLevel.FATAL;
            default -> ParsedLogEvent.LogLevel.INFO;
        };
    }

    private Instant parseTimestamp(String timestamp) {
        if (timestamp == null) return Instant.now();
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
