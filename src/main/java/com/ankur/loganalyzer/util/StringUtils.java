package com.ankur.loganalyzer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern EXCEPTION_PATTERN = Pattern.compile(
            "^(\\w+(\\.\\w+)*Exception|\\w+(\\.\\w+)*Error)");
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile(
            "[a-f0-9]{32}|[a-f0-9]{16}");

    public static boolean isBlankOrNull(String str) {
        return str == null || str.isBlank();
    }

    public static String truncate(String str, int length) {
        if (str == null || str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }

    public static String extractExceptionType(String logLine) {
        if (logLine == null || logLine.isBlank()) {
            return null;
        }

        Matcher matcher = EXCEPTION_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public static String extractTraceId(String logLine) {
        if (logLine == null || logLine.isBlank()) {
            return null;
        }

        Matcher matcher = TRACE_ID_PATTERN.matcher(logLine);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public static String sanitizeForSearch(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("[^a-zA-Z0-9 ._-]", " ").trim();
    }

    public static int countOccurrences(String text, String pattern) {
        if (text == null || pattern == null || pattern.isEmpty()) {
            return 0;
        }
        return text.split(Pattern.quote(pattern), -1).length - 1;
    }
}
