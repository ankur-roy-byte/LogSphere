package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.client.LokiClientService;
import com.ankur.loganalyzer.dto.LogUploadResponse;
import com.ankur.loganalyzer.dto.LokiFetchRequest;
import com.ankur.loganalyzer.model.LogSource;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import com.ankur.loganalyzer.service.LogIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs/fetch")
@RequiredArgsConstructor
public class LokiFetchController {

    private final LokiClientService lokiClientService;
    private final LogIngestionService logIngestionService;
    private final LogSourceRepository logSourceRepository;

    @PostMapping("/loki")
    public ResponseEntity<LogUploadResponse> fetchFromLoki(@Valid @RequestBody LokiFetchRequest request) {
        List<String> logLines = lokiClientService.queryRange(
                request.query(), request.startNs(), request.endNs(), request.limit());

        if (logLines.isEmpty()) {
            return ResponseEntity.ok(LogUploadResponse.builder()
                    .totalLines(0)
                    .parsedSuccessfully(0)
                    .parseFailures(0)
                    .message("No logs found in Loki for the given query")
                    .build());
        }

        LogSource source = logSourceRepository.findByName("loki")
                .orElseGet(() -> logSourceRepository.save(
                        LogSource.builder()
                                .name("loki")
                                .type(LogSource.SourceType.LOKI)
                                .build()));

        LogIngestionService.IngestionResult result = logIngestionService.ingestRawLines(logLines, source);

        return ResponseEntity.ok(LogUploadResponse.builder()
                .totalLines(result.totalLines())
                .parsedSuccessfully(result.parsedSuccessfully())
                .parseFailures(result.parseFailures())
                .message("Loki log fetch and ingestion complete")
                .build());
    }
}
