package com.ankur.loganalyzer.parser;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(2)
public class RegexLogParser implements LogParser {

    // Matches common log formats:
    // 2024-01-15 10:30:45.123 ERROR [service-name] [thread-1] c.e.MyClass - Something failed
    // 2024-01-15T10:30:45.123Z ERROR service-name --- [thread-1] c.e.MyClass : Something failed
    private static final Pattern SPRING_LOG_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}[.\\d]*)\\s*Z?\\s+" +
            "(TRACE|DEBUG|INFO|WARN|ERROR|FATAL)\\s+" +
            "(?:\\[?([\\w.-]+)]?)?\\s*(?:---)?\\s*" +
            "(?:\\[([^]]+)])?\\s*" +
            "(?:[\\w.$]+)?\\s*[:\\-]?\\s*" +
            "(.*)",
            Pattern.DOTALL
    );

    // Matches: [2024-01-15 10:30:45] ERROR: Something failed
    private static final Pattern BRACKET_LOG_PATTERN = Pattern.compile(
            "^\\[(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}[.\\d]*)\\]\\s+" +
            "(TRACE|DEBUG|INFO|WARN|ERROR|FATAL):?\\s+(.*)",
            Pattern.DOTALL
    );

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][' ']HH:mm:ss[.SSS][.SS][.S]");

    @Override
    public boolean supports(String rawLog) {
        if (rawLog == null || rawLog.isBlank()) return false;
        return SPRING_LOG_PATTERN.matcher(rawLog).matches()
                || BRACKET_LOG_PATTERN.matcher(rawLog).matches();
    }

    @Override
    public ParsedLogEvent.ParsedLogEventBuilder parse(String rawLog) {
        Matcher springMatcher = SPRING_LOG_PATTERN.matcher(rawLog);
        if (springMatcher.matches()) {
            return parseSpringLog(springMatcher);
        }

        Matcher bracketMatcher = BRACKET_LOG_PATTERN.matcher(rawLog);
        if (bracketMatcher.matches()) {
            return parseBracketLog(bracketMatcher);
        }

        return ParsedLogEvent.builder()
                .level(ParsedLogEvent.LogLevel.INFO)
                .message(rawLog)
                .timestamp(Instant.now());
    }

    private ParsedLogEvent.ParsedLogEventBuilder parseSpringLog(Matcher matcher) {
        String timestampStr = matcher.group(1);
        String level = matcher.group(2);
        String service = matcher.group(3);
        String thread = matcher.group(4);
        String message = matcher.group(5);

        String exceptionType = extractExceptionType(message);

        return ParsedLogEvent.builder()
                .timestamp(parseTimestamp(timestampStr))
                .level(ParsedLogEvent.LogLevel.valueOf(level))
                .serviceName(service)
                .message(message != null ? message.trim() : "")
                .exceptionType(exceptionType)
                .metadata(thread != null ? Map.of("thread", thread) : null);
    }

    private ParsedLogEvent.ParsedLogEventBuilder parseBracketLog(Matcher matcher) {
        String timestampStr = matcher.group(1);
        String level = matcher.group(2);
        String message = matcher.group(3);

        return ParsedLogEvent.builder()
                .timestamp(parseTimestamp(timestampStr))
                .level(ParsedLogEvent.LogLevel.valueOf(level))
                .message(message != null ? message.trim() : "");
    }

    private Instant parseTimestamp(String timestamp) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(timestamp, DATETIME_FORMATTER);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    private String extractExceptionType(String message) {
        if (message == null) return null;
        Pattern exPattern = Pattern.compile("([a-zA-Z][\\w.]*(?:Exception|Error|Throwable))");
        Matcher m = exPattern.matcher(message);
        return m.find() ? m.group(1) : null;
    }
}
