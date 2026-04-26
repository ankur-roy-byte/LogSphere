package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.service.AsyncLogProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Async endpoints for long-running operations.
 * Returns 202 Accepted while processing continues in background.
 */
@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Async Operations", description = "Non-blocking endpoints for long-running tasks")
public class AsyncController {

    private final AsyncLogProcessingService asyncLogService;

    @PostMapping("/logs/process")
    @Operation(summary = "Process logs asynchronously", description = "Start async processing without blocking")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Processing started"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid service name")
    })
    public DeferredResult<ResponseEntity<ApiResponse<String>>> processLogsAsync(
            @Parameter(description = "Service name", required = true)
            @RequestParam String serviceName,
            @Parameter(description = "Simulated delay in milliseconds")
            @RequestParam(defaultValue = "0") int delayMs) {
        
        DeferredResult<ResponseEntity<ApiResponse<String>>> result = new DeferredResult<>(30000L);
        
        asyncLogService.processLogsAsync(serviceName, delayMs)
            .thenAccept(processResult -> {
                log.info("Async processing completed for {}", serviceName);
                result.setResult(
                    ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success(processResult, "Async processing completed"))
                );
            })
            .exceptionally(ex -> {
                log.error("Async processing failed", ex);
                result.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(500, "Async processing failed", ex.getMessage()))
                );
                return null;
            });

        return result;
    }

    @PostMapping("/reports/generate")
    @Operation(summary = "Generate report asynchronously", description = "Generate report without blocking")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Report generation started"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid parameters")
    })
    public DeferredResult<ResponseEntity<ApiResponse<String>>> generateReportAsync(
            @Parameter(description = "Service name", required = true)
            @RequestParam String serviceName,
            @Parameter(description = "Report type")
            @RequestParam(defaultValue = "summary") String reportType) {
        
        DeferredResult<ResponseEntity<ApiResponse<String>>> result = new DeferredResult<>(60000L);
        
        asyncLogService.generateReportAsync(serviceName, reportType)
            .thenAccept(reportResult -> {
                log.info("Report generation completed for {}", serviceName);
                result.setResult(
                    ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success(reportResult, "Report ready"))
                );
            })
            .exceptionally(ex -> {
                log.error("Report generation failed", ex);
                result.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(500, "Report generation failed", ex.getMessage()))
                );
                return null;
            });
        
        return result;
    }

    @PostMapping("/analysis/complex-workflow")
    @Operation(summary = "Execute complex analysis workflow", description = "Run multi-step analysis asynchronously")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Workflow started"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid service name")
    })
    public DeferredResult<ResponseEntity<ApiResponse<String>>> complexAnalysisWorkflowAsync(
            @Parameter(description = "Service name", required = true)
            @RequestParam String serviceName) {
        
        DeferredResult<ResponseEntity<ApiResponse<String>>> result = new DeferredResult<>(120000L);
        
        asyncLogService.complexAnalysisWorkflowAsync(serviceName)
            .thenAccept(workflowResult -> {
                log.info("Complex workflow completed for {}", serviceName);
                result.setResult(
                    ResponseEntity.status(HttpStatus.OK)
                        .body(ApiResponse.success(workflowResult, "Workflow completed"))
                );
            })
            .exceptionally(ex -> {
                log.error("Complex workflow failed", ex);
                result.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(500, "Workflow execution failed", ex.getMessage()))
                );
                return null;
            });
        
        return result;
    }

    @GetMapping("/health")
    @Operation(summary = "Check async system health")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Async system is healthy")
    public ResponseEntity<ApiResponse<Object>> checkAsyncHealth() {
        return ResponseEntity.ok(ApiResponse.success(
            new Object() {
                public final String status = "healthy";
                public final String message = "Async thread pools operational";
            },
            "Async service ready"
        ));
    }
}
