package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.ExceptionTrendResponse;
import com.ankur.loganalyzer.service.ExceptionTrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs/exceptions")
@RequiredArgsConstructor
@Tag(name = "Exception Trends", description = "Top exception types within a time window")
public class ExceptionTrendController {

    private final ExceptionTrendService exceptionTrendService;

    @GetMapping
    @Operation(summary = "Top exception types", description = "Returns the most frequent exception types seen in parsed logs within the given time window")
    public ResponseEntity<ApiResponse<ExceptionTrendResponse>> getTrend(
            @RequestParam(defaultValue = "60") long windowMinutes,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(ApiResponse.success(exceptionTrendService.getTrend(windowMinutes, limit)));
    }
}
