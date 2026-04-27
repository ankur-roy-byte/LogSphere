package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.service.LogRetentionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/retention")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Log Retention", description = "Manage log data lifecycle and retention policies")
public class RetentionController {

    private final LogRetentionService retentionService;

    @GetMapping("/status")
    @Operation(
        summary = "Get retention status",
        description = "Returns current retention configuration and total log counts per tier"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Retention status retrieved")
    })
    public ResponseEntity<ApiResponse<LogRetentionService.RetentionStatus>> getStatus() {
        return ResponseEntity.ok(
            ApiResponse.success(retentionService.getStatus(), "Retention status retrieved")
        );
    }

    @PostMapping("/purge")
    @Operation(
        summary = "Trigger manual retention purge",
        description = "Immediately purges logs older than the configured retention period for each tier"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Purge completed"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Purge skipped — retention disabled")
    })
    public ResponseEntity<ApiResponse<LogRetentionService.RetentionResult>> triggerPurge() {
        log.info("Manual retention purge triggered");
        LogRetentionService.RetentionResult result = retentionService.purgeOldLogs();
        String message = result.executed()
            ? String.format("Purge complete: %d parsed, %d raw, %d analysis records removed",
                result.parsedLogsDeleted(), result.rawLogsDeleted(), result.analysisResultsDeleted())
            : "Purge skipped — retention is disabled";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }
}
