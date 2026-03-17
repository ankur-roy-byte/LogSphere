package com.ankur.loganalyzer.parser;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(3)
public class StackTraceParser implements LogParser {

    private static final Pattern STACKTRACE_PATTERN = Pattern.compile(
            "^((?:[a-zA-Z][\\w.]*(?:Exception|Error|Throwable))(?::\\s*.+)?)\\s*" +
            "((?:\\s+at\\s+.+\\n?)+.*)",
            Pattern.MULTILINE | Pattern.DOTALL
    );

    private static final Pattern EXCEPTION_LINE_PATTERN = Pattern.compile(
            "([a-zA-Z][\\w.]*(?:Exception|Error|Throwable))(?::\\s*(.+))?"
    );

    @Override
    public boolean supports(String rawLog) {
        if (rawLog == null || rawLog.isBlank()) return false;
        return rawLog.contains("\tat ") || rawLog.contains("\n\tat ")
                || STACKTRACE_PATTERN.matcher(rawLog).find();
    }

    @Override
    public ParsedLogEvent.ParsedLogEventBuilder parse(String rawLog) {
        String exceptionType = null;
        String message = rawLog;

        Matcher exMatcher = EXCEPTION_LINE_PATTERN.matcher(rawLog);
        if (exMatcher.find()) {
            exceptionType = exMatcher.group(1);
            String exMsg = exMatcher.group(2);
            if (exMsg != null) {
                message = exceptionType + ": " + exMsg;
            }
        }

        return ParsedLogEvent.builder()
                .level(ParsedLogEvent.LogLevel.ERROR)
                .message(message)
                .exceptionType(exceptionType)
                .stackTrace(rawLog)
                .timestamp(Instant.now());
    }
}
