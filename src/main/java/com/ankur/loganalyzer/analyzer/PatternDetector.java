package com.ankur.loganalyzer.analyzer;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Detects patterns in log messages to group similar errors and identify common issues
 */
@Component
public class PatternDetector {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}");

    /**
     * Extracts a pattern template from a log message by replacing variable parts
     */
    public String extractPattern(String message) {
        if (message == null || message.isBlank()) {
            return "EMPTY_MESSAGE";
        }

        String pattern = message;

        // Replace UUIDs
        pattern = UUID_PATTERN.matcher(pattern).replaceAll("<UUID>");

        // Replace timestamps
        pattern = TIMESTAMP_PATTERN.matcher(pattern).replaceAll("<TIMESTAMP>");

        // Replace numbers (but not in words)
        pattern = NUMBER_PATTERN.matcher(pattern).replaceAll("<NUM>");

        // Replace hex values
        pattern = pattern.replaceAll("0x[0-9a-fA-F]+", "<HEX>");

        // Replace file paths
        pattern = pattern.replaceAll("/[\\w/.-]+\\.(java|py|js|ts|go|rb|php)", "<FILEPATH>");

        // Replace IP addresses
        pattern = pattern.replaceAll("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "<IP>");

        return pattern;
    }

    /**
     * Groups log events by their extracted patterns
     */
    public Map<String, List<ParsedLogEvent>> groupByPattern(List<ParsedLogEvent> logEvents) {
        Map<String, List<ParsedLogEvent>> patternGroups = new HashMap<>();

        for (ParsedLogEvent event : logEvents) {
            String pattern = extractPattern(event.getMessage());
            patternGroups.computeIfAbsent(pattern, k -> new ArrayList<>()).add(event);
        }

        return patternGroups;
    }

    /**
     * Finds the most common patterns in the provided log events
     */
    public List<PatternOccurrence> findTopPatterns(List<ParsedLogEvent> logEvents, int limit) {
        Map<String, List<ParsedLogEvent>> grouped = groupByPattern(logEvents);

        return grouped.entrySet().stream()
                .map(entry -> new PatternOccurrence(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().get(0).getMessage()
                ))
                .sorted(Comparator.comparingInt(PatternOccurrence::getCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Detects similar stack traces by comparing their root causes
     */
    public String extractStackTraceSignature(String stackTrace) {
        if (stackTrace == null || stackTrace.isBlank()) {
            return "NO_STACK_TRACE";
        }

        // Extract the first few lines of the stack trace (the root cause)
        String[] lines = stackTrace.split("\\n");
        StringBuilder signature = new StringBuilder();

        int linesToInclude = Math.min(3, lines.length);
        for (int i = 0; i < linesToInclude; i++) {
            String line = lines[i].trim();
            // Remove line numbers and file details
            line = line.replaceAll(":\\d+\\)", ")");
            line = line.replaceAll("\\(.*?\\)", "()");
            signature.append(line).append("\n");
        }

        return signature.toString().trim();
    }

    public static class PatternOccurrence {
        private final String pattern;
        private final int count;
        private final String exampleMessage;

        public PatternOccurrence(String pattern, int count, String exampleMessage) {
            this.pattern = pattern;
            this.count = count;
            this.exampleMessage = exampleMessage;
        }

        public String getPattern() { return pattern; }
        public int getCount() { return count; }
        public String getExampleMessage() { return exampleMessage; }
    }
}
