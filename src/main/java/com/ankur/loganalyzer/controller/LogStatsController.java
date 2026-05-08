package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.LogStatsResponse;
import com.ankur.loganalyzer.service.LogStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs/stats")
@RequiredArgsConstructor
@Tag(name = "Log Statistics", description = "Per-level and per-service log breakdown")
public class LogStatsController {

    private final LogStatsService logStatsService;

    @GetMapping
    @Operation(
        summary = "Get log statistics",
        description = "Returns level breakdown, top error services, and top volume services within a sliding time window"
    )
    public ResponseEntity<ApiResponse<LogStatsResponse>> getStats(
            @RequestParam(defaultValue = "60") long windowMinutes) {
        return ResponseEntity.ok(ApiResponse.success(logStatsService.getStats(windowMinutes)));
    }
}
