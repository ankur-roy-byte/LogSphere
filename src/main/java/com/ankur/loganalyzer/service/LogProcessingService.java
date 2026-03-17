package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.parser.ParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogProcessingService {

    private final ParserFactory parserFactory;
    @Qualifier("parsingExecutor")
    private final Executor parsingExecutor;
    @Qualifier("analysisExecutor")
    private final Executor analysisExecutor;

    public List<ParsedLogEvent> parseLogsConcurrently(List<String> rawLogs) {
        List<CompletableFuture<ParsedLogEvent>> futures = rawLogs.stream()
                .map(log -> CompletableFuture.supplyAsync(
                        () -> parserFactory.parse(log).build(), parsingExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    public CompletableFuture<List<ParsedLogEvent>> parseLogsConcurrentlyAsync(List<String> rawLogs) {
        List<CompletableFuture<ParsedLogEvent>> futures = rawLogs.stream()
                .map(log -> CompletableFuture.supplyAsync(
                        () -> parserFactory.parse(log).build(), parsingExecutor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList(), analysisExecutor);
    }

    public List<List<ParsedLogEvent>> parseBatchesConcurrently(List<String> rawLogs, int batchSize) {
        List<List<String>> batches = partition(rawLogs, batchSize);
        List<CompletableFuture<List<ParsedLogEvent>>> batchFutures = new ArrayList<>();

        for (List<String> batch : batches) {
            CompletableFuture<List<ParsedLogEvent>> future = CompletableFuture.supplyAsync(
                    () -> batch.stream()
                            .map(log -> parserFactory.parse(log).build())
                            .toList(),
                    parsingExecutor);
            batchFutures.add(future);
        }

        return batchFutures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    private static <T> List<List<T>> partition(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }
}
