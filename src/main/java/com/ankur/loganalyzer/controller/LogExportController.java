package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.service.LogExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Download log data as CSV or JSON")
public class LogExportController {

    private final LogExportService logExportService;

    @GetMapping(value = "/logs.csv", produces = "text/csv")
    @Operation(summary = "Export logs as CSV", description = "Download parsed log events as a CSV file (max 10,000 rows)")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) ParsedLogEvent.LogLevel level,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        byte[] csv = logExportService.exportAsCsv(level, serviceName, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping(value = "/logs.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Export logs as JSON", description = "Download parsed log events as a JSON array (max 10,000 rows)")
    public ResponseEntity<byte[]> exportJson(
            @RequestParam(required = false) ParsedLogEvent.LogLevel level,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) throws IOException {

        byte[] json = logExportService.exportAsJson(level, serviceName, from, to);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"logs.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
