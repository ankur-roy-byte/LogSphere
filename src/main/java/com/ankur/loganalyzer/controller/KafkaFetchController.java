package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.KafkaFetchRequest;
import com.ankur.loganalyzer.dto.LogUploadResponse;
import com.ankur.loganalyzer.model.LogSource;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import com.ankur.loganalyzer.service.KafkaFetchService;
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
public class KafkaFetchController {

    private final KafkaFetchService kafkaFetchService;
    private final LogIngestionService logIngestionService;
    private final LogSourceRepository logSourceRepository;

    @PostMapping("/kafka")
    public ResponseEntity<LogUploadResponse> fetchFromKafka(@Valid @RequestBody KafkaFetchRequest request) {
        List<String> logLines = kafkaFetchService.fetch(
                request.bootstrapServers(),
                request.topic(),
                request.groupId(),
                request.resolvedLimit());

        if (logLines.isEmpty()) {
            return ResponseEntity.ok(LogUploadResponse.builder()
                    .totalLines(0)
                    .parsedSuccessfully(0)
                    .parseFailures(0)
                    .message("No logs fetched from Kafka topic")
                    .build());
        }

        LogSource source = logSourceRepository.findByName("kafka")
                .orElseGet(() -> logSourceRepository.save(
                        LogSource.builder()
                                .name("kafka")
                                .type(LogSource.SourceType.KAFKA)
                                .build()));

        LogIngestionService.IngestionResult result = logIngestionService.ingestRawLines(logLines, source);

        return ResponseEntity.ok(LogUploadResponse.builder()
                .totalLines(result.totalLines())
                .parsedSuccessfully(result.parsedSuccessfully())
                .parseFailures(result.parseFailures())
                .message("Kafka log fetch and ingestion complete")
                .build());
    }
}
