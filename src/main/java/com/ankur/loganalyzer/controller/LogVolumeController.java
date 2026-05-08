package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.LogVolumeResponse;
import com.ankur.loganalyzer.service.LogVolumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/logs/volume")
@RequiredArgsConstructor
@Tag(name = "Log Volume", description = "Hourly and daily log volume breakdown")
public class LogVolumeController {

    private final LogVolumeService logVolumeService;

    @GetMapping
    @Operation(
        summary = "Get log volume over time",
        description = "Returns event counts grouped by hour or day within the specified time range"
    )
    public ResponseEntity<ApiResponse<LogVolumeResponse>> getVolume(
            @RequestParam(defaultValue = "hour") String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        Instant end   = to   != null ? to   : Instant.now();
        Instant start = from != null ? from : end.minus(24, ChronoUnit.HOURS);

        return ResponseEntity.ok(ApiResponse.success(logVolumeService.getVolume(granularity, start, end)));
    }
}
