package com.ankur.loganalyzer.dto;

import java.time.Instant;
import java.util.List;

public record ExceptionTrendResponse(
        List<Entry> exceptions,
        long windowMinutes,
        Instant windowStart,
        Instant windowEnd
) {
    public record Entry(String exceptionType, long count) {}
}
