package com.ankur.loganalyzer.dto;

import lombok.Builder;

@Builder
public record LogUploadResponse(
        int totalLines,
        int parsedSuccessfully,
        int parseFailures,
        String message
) {
}
