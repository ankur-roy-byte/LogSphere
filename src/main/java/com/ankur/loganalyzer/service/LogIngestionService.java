package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.annotation.MetricCategory;
import com.ankur.loganalyzer.annotation.Tracked;
import com.ankur.loganalyzer.model.LogSource;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.model.RawLogEvent;
import com.ankur.loganalyzer.parser.ParserFactory;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogIngestionService {

    private final RawLogEventRepository rawLogEventRepository;
    private final ParsedLogEventRepository parsedLogEventRepository;
    private final LogSourceRepository logSourceRepository;
    private final ParserFactory parserFactory;
    @Qualifier("ingestionExecutor")
    private final Executor ingestionExecutor;
    @Qualifier("parsingExecutor")
    private final Executor parsingExecutor;

    public record IngestionResult(int totalLines, int parsedSuccessfully, int parseFailures) {}

    @Tracked(category = MetricCategory.INGESTION, operation = "upload")
    public IngestionResult ingestFromUpload(String content, String sourceName) {
        LogSource source = logSourceRepository.findByName(sourceName != null ? sourceName : "file-upload")
                .orElseGet(() -> logSourceRepository.save(
                        LogSource.builder()
                                .name(sourceName != null ? sourceName : "file-upload")
                                .type(LogSource.SourceType.FILE)
                                .build()
                ));

        String[] lines = content.split("\\n");
        List<String> rawLines = new ArrayList<>();
        StringBuilder multiLineBuffer = new StringBuilder();

        // Group multiline stack traces together
        for (String line : lines) {
            if (line.isBlank()) continue;
            if (line.startsWith("\t") || line.startsWith("    at ") || line.startsWith("Caused by:")) {
                multiLineBuffer.append("\n").append(line);
            } else {
                if (!multiLineBuffer.isEmpty()) {
                    rawLines.add(multiLineBuffer.toString());
                    multiLineBuffer.setLength(0);
                }
                multiLineBuffer.append(line);
            }
        }
        if (!multiLineBuffer.isEmpty()) {
            rawLines.add(multiLineBuffer.toString());
        }

        return processBatch(rawLines, source);
    }

    public IngestionResult ingestRawLines(List<String> rawLines, LogSource source) {
        return processBatch(rawLines, source);
    }

    private IngestionResult processBatch(List<String> rawLines, LogSource source) {
        int totalLines = rawLines.size();
        int parsedOk = 0;
        int parseFailed = 0;

        // Partition into batches of 500 for concurrent processing
        List<List<String>> batches = partition(rawLines, 500);
        List<CompletableFuture<BatchResult>> futures = new ArrayList<>();

        for (List<String> batch : batches) {
            CompletableFuture<BatchResult> future = CompletableFuture.supplyAsync(
                    () -> processSingleBatch(batch, source), parsingExecutor);
            futures.add(future);
        }

        // Collect results
        for (CompletableFuture<BatchResult> future : futures) {
            BatchResult result = future.join();
            parsedOk += result.success;
            parseFailed += result.failures;
        }

        log.info("Ingestion complete: total={}, parsed={}, failed={}", totalLines, parsedOk, parseFailed);
        return new IngestionResult(totalLines, parsedOk, parseFailed);
    }

    private BatchResult processSingleBatch(List<String> batch, LogSource source) {
        int success = 0;
        int failures = 0;
        Instant now = Instant.now();

        for (String rawLine : batch) {
            try {
                RawLogEvent rawEvent = rawLogEventRepository.save(
                        RawLogEvent.builder()
                                .source(source)
                                .rawMessage(rawLine)
                                .timestamp(now)
                                .ingestionTime(now)
                                .build()
                );

                ParsedLogEvent.ParsedLogEventBuilder builder = parserFactory.parse(rawLine);
                ParsedLogEvent parsed = builder
                        .rawEvent(rawEvent)
                        .build();

                parsedLogEventRepository.save(parsed);
                success++;
            } catch (Exception e) {
                log.warn("Failed to parse log line: {}", rawLine.substring(0, Math.min(100, rawLine.length())), e);
                failures++;
            }
        }
        return new BatchResult(success, failures);
    }

    private record BatchResult(int success, int failures) {}

    private static <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }
}
