package com.ankur.loganalyzer.parser;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ParserFactory {

    private final List<LogParser> parsers;

    public ParserFactory(List<LogParser> parsers) {
        this.parsers = parsers;
    }

    public ParsedLogEvent.ParsedLogEventBuilder parse(String rawLog) {
        for (LogParser parser : parsers) {
            if (parser.supports(rawLog)) {
                return parser.parse(rawLog);
            }
        }
        // fallback: treat as plain text INFO log
        return ParsedLogEvent.builder()
                .level(ParsedLogEvent.LogLevel.INFO)
                .message(rawLog)
                .timestamp(java.time.Instant.now());
    }
}
