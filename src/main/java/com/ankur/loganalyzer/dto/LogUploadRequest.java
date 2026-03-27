package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.validation.MaxSize;
import com.ankur.loganalyzer.validation.ValidLogFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record LogUploadRequest(
        @NotBlank(message = "Log content is required")
        @MaxSize(value = 10485760, message = "Log content cannot exceed 10MB")
        String content,

        @Size(min = 1, max = 100, message = "Source name must be between 1 and 100 characters")
        String sourceName,

        @ValidLogFormat(message = "Invalid log format. Supported: json, regex, stacktrace, text")
        String format
) {
}
