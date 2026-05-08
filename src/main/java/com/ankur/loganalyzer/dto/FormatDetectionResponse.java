package com.ankur.loganalyzer.dto;

public record FormatDetectionResponse(
        String format,
        String confidence,
        String hint
) {}
