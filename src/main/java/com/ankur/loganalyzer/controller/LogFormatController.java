package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.FormatDetectionRequest;
import com.ankur.loganalyzer.dto.FormatDetectionResponse;
import com.ankur.loganalyzer.parser.JsonLogParser;
import com.ankur.loganalyzer.parser.StackTraceParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs/detect-format")
@RequiredArgsConstructor
@Tag(name = "Log Format Detection", description = "Detect the format of a log sample before ingestion")
public class LogFormatController {

    private final JsonLogParser jsonLogParser;
    private final StackTraceParser stackTraceParser;

    @PostMapping
    @Operation(summary = "Detect log format", description = "Analyses a sample log line and returns the most likely parser format")
    public ResponseEntity<ApiResponse<FormatDetectionResponse>> detect(
            @Valid @RequestBody FormatDetectionRequest request) {

        String sample = request.sample().trim();
        FormatDetectionResponse result;

        if (jsonLogParser.supports(sample)) {
            result = new FormatDetectionResponse("JSON", "HIGH",
                    "Sample looks like structured JSON — use the JSON parser for best field extraction");
        } else if (stackTraceParser.supports(sample)) {
            result = new FormatDetectionResponse("STACK_TRACE", "HIGH",
                    "Sample contains a Java stack trace — exception type and frames will be extracted");
        } else if (sample.matches(".*\\d{4}-\\d{2}-\\d{2}.*")) {
            result = new FormatDetectionResponse("REGEX", "MEDIUM",
                    "Sample appears to be a timestamped plain-text log — regex parser will extract level and message");
        } else {
            result = new FormatDetectionResponse("PLAIN_TEXT", "LOW",
                    "No known format detected — log will be stored as a plain INFO message");
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
