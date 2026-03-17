package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.LogUploadRequest;
import com.ankur.loganalyzer.dto.LogUploadResponse;
import com.ankur.loganalyzer.dto.ParsedLogResponse;
import com.ankur.loganalyzer.service.LogIngestionService;
import com.ankur.loganalyzer.service.LogSearchService;
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
public class LogController {

    private final LogIngestionService logIngestionService;
    private final LogSearchService logSearchService;

    @PostMapping("/upload")
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
    public ResponseEntity<Page<ParsedLogResponse>> searchLogs(
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String host,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(logSearchService.searchLogs(
                serviceName, level, traceId, keyword, host,
                startTime, endTime, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParsedLogResponse> getLogById(@PathVariable Long id) {
        return ResponseEntity.ok(logSearchService.getById(id));
    }
}
