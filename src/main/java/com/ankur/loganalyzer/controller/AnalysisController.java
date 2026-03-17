package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AnalysisSummaryResponse;
import com.ankur.loganalyzer.dto.SpikeDetectionResponse;
import com.ankur.loganalyzer.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final LogAnalysisService logAnalysisService;

    @GetMapping("/summary")
    public ResponseEntity<AnalysisSummaryResponse> getSummary(
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime) {
        return ResponseEntity.ok(logAnalysisService.generateSummary(startTime, endTime));
    }

    @GetMapping("/errors/top")
    public ResponseEntity<List<Map<String, Object>>> getTopExceptions(
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(defaultValue = "10") int limit) {

        List<Object[]> results = logAnalysisService.getTopExceptions(startTime, endTime, limit);
        List<Map<String, Object>> response = results.stream()
                .map(row -> Map.<String, Object>of(
                        "exceptionType", row[0].toString(),
                        "count", (Long) row[1]))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/spikes")
    public ResponseEntity<SpikeDetectionResponse> detectSpikes() {
        return ResponseEntity.ok(logAnalysisService.detectSpikes());
    }

    @GetMapping("/exceptions")
    public ResponseEntity<List<Map<String, Object>>> getRepeatedMessages(
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @RequestParam(defaultValue = "3") long minCount,
            @RequestParam(defaultValue = "20") int limit) {

        List<Object[]> results = logAnalysisService.getRepeatedMessages(startTime, endTime, minCount, limit);
        List<Map<String, Object>> response = results.stream()
                .map(row -> Map.<String, Object>of(
                        "message", row[0].toString(),
                        "count", (Long) row[1]))
                .toList();
        return ResponseEntity.ok(response);
    }
}
