package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AlertStatsResponse;
import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.service.AlertStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alerts/stats")
@RequiredArgsConstructor
@Tag(name = "Alert Statistics", description = "Alert trend and rule firing summary")
public class AlertStatsController {

    private final AlertStatsService alertStatsService;

    @GetMapping
    @Operation(
        summary = "Get alert statistics",
        description = "Returns rule counts, active vs resolved totals, and the top 10 firing rules within the given time window"
    )
    public ResponseEntity<ApiResponse<AlertStatsResponse>> getStats(
            @RequestParam(defaultValue = "1440") long windowMinutes) {
        return ResponseEntity.ok(ApiResponse.success(alertStatsService.getStats(windowMinutes)));
    }
}
