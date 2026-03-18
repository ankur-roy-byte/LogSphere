package com.ankur.loganalyzer.analyzer;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Classifies errors based on severity, exception type, and patterns
 */
@Component
public class ErrorClassifier {

    public ErrorClassification classifyError(ParsedLogEvent logEvent) {
        if (logEvent == null || logEvent.getLevel() == null) {
            return new ErrorClassification("UNKNOWN", "INFO", "Unclassified");
        }

        String severity = determineSeverity(logEvent);
        String category = categorizeError(logEvent);
        String description = generateDescription(logEvent);

        return new ErrorClassification(category, severity, description);
    }

    private String determineSeverity(ParsedLogEvent logEvent) {
        return switch (logEvent.getLevel()) {
            case FATAL -> "CRITICAL";
            case ERROR -> "HIGH";
            case WARN -> "MEDIUM";
            case DEBUG, TRACE -> "LOW";
            default -> "INFO";
        };
    }

    private String categorizeError(ParsedLogEvent logEvent) {
        String exceptionType = logEvent.getExceptionType();
        String message = logEvent.getMessage();

        if (exceptionType != null) {
            if (exceptionType.contains("NullPointer")) return "NULL_POINTER";
            if (exceptionType.contains("OutOfMemory")) return "MEMORY";
            if (exceptionType.contains("StackOverflow")) return "MEMORY";
            if (exceptionType.contains("SQL") || exceptionType.contains("Database")) return "DATABASE";
            if (exceptionType.contains("IO") || exceptionType.contains("File")) return "IO";
            if (exceptionType.contains("Network") || exceptionType.contains("Connection")) return "NETWORK";
            if (exceptionType.contains("Timeout")) return "TIMEOUT";
            if (exceptionType.contains("Security") || exceptionType.contains("Auth")) return "SECURITY";
            return "APPLICATION";
        }

        if (message != null) {
            String lowerMsg = message.toLowerCase();
            if (lowerMsg.contains("out of memory") || lowerMsg.contains("heap space")) return "MEMORY";
            if (lowerMsg.contains("timeout") || lowerMsg.contains("timed out")) return "TIMEOUT";
            if (lowerMsg.contains("connection") || lowerMsg.contains("network")) return "NETWORK";
            if (lowerMsg.contains("database") || lowerMsg.contains("sql")) return "DATABASE";
            if (lowerMsg.contains("permission") || lowerMsg.contains("unauthorized")) return "SECURITY";
        }

        return "GENERAL";
    }

    private String generateDescription(ParsedLogEvent logEvent) {
        if (logEvent.getExceptionType() != null) {
            return String.format("%s exception occurred", logEvent.getExceptionType());
        }
        return logEvent.getMessage() != null ? logEvent.getMessage() : "No description available";
    }

    public static class ErrorClassification {
        private final String category;
        private final String severity;
        private final String description;

        public ErrorClassification(String category, String severity, String description) {
            this.category = category;
            this.severity = severity;
            this.description = description;
        }

        public String getCategory() { return category; }
        public String getSeverity() { return severity; }
        public String getDescription() { return description; }
    }
}
