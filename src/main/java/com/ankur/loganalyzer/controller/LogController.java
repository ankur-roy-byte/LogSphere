package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.LogUploadRequest;
import com.ankur.loganalyzer.dto.LogUploadResponse;
import com.ankur.loganalyzer.dto.ParsedLogResponse;
import com.ankur.loganalyzer.service.LogIngestionService;
import com.ankur.loganalyzer.service.LogSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Tag(name = "Log Management", description = "APIs for uploading, searching, and retrieving logs")
public class LogController {

    private final LogIngestionService logIngestionService;
    private final LogSearchService logSearchService;

    @PostMapping("/upload")
    @Operation(summary = "Upload logs",
            description = "Upload raw log entries in text format for ingestion and parsing")
    @ApiResponse(responseCode = "200", description = "Logs uploaded successfully",
            content = @Content(schema = @Schema(implementation = LogUploadResponse.class)))
    public ResponseEntity<LogUploadResponse> uploadLogs(@Valid @RequestBody LogUploadRequest request) {
        LogIngestionService.IngestionResult result = logIngestionService.ingestFromUpload(
                request.content(), request.sourceName());

        return ResponseEntity.ok(LogUploadResponse.builder()
                .totalLines(result.totalLines())
                .parsedSuccessfully(result.parsedSuccessfully())
                .parseFailures(result.parseFailures())
                .message("Log ingestion complete")
                .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search logs",
            description = "Search parsed logs with multiple filter criteria")
    @ApiResponse(responseCode = "200", description = "Search results returned successfully")
    public ResponseEntity<Page<ParsedLogResponse>> searchLogs(
            @Parameter(description = "Service name filter") @RequestParam(required = false) String serviceName,
            @Parameter(description = "Log level filter (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)") @RequestParam(required = false) String level,
            @Parameter(description = "Trace ID filter") @RequestParam(required = false) String traceId,
            @Parameter(description = "Keyword search in message") @RequestParam(required = false) String keyword,
            @Parameter(description = "Host filter") @RequestParam(required = false) String host,
            @Parameter(description = "Start time (ISO-8601)") @RequestParam(required = false) Instant startTime,
            @Parameter(description = "End time (ISO-8601)") @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")@RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(logSearchService.searchLogs(
                serviceName, level, traceId, keyword, host,
                startTime, endTime, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get log by ID", description = "Retrieve a specific parsed log entry by its ID")
    @ApiResponse(responseCode = "200", description = "Log found and returned")
    @ApiResponse(responseCode = "404", description = "Log not found")
    public ResponseEntity<ParsedLogResponse> getLogById(
            @Parameter(description = "Log entry ID") @PathVariable Long id) {
        return ResponseEntity.ok(logSearchService.getById(id));
    }
}

