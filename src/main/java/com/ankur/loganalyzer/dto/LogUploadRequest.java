package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LogUploadRequest(
        @NotBlank(message = "Log content is required")
        String content,

        String sourceName,

        String format
) {
}
