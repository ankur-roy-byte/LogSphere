package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.LogSourceRequest;
import com.ankur.loganalyzer.dto.LogSourceResponse;
import com.ankur.loganalyzer.service.LogSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
@Tag(name = "Log Sources", description = "Manage ingestion source registrations")
public class LogSourceController {

    private final LogSourceService logSourceService;

    @GetMapping
    @Operation(summary = "List all log sources")
    public ResponseEntity<ApiResponse<List<LogSourceResponse>>> list(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<LogSourceResponse> sources = activeOnly
                ? logSourceService.listActive()
                : logSourceService.listAll();
        return ResponseEntity.ok(ApiResponse.success(sources));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get log source by ID")
    public ResponseEntity<ApiResponse<LogSourceResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(logSourceService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Register a new log source")
    public ResponseEntity<ApiResponse<LogSourceResponse>> create(@Valid @RequestBody LogSourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(logSourceService.create(request)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a log source")
    public ResponseEntity<ApiResponse<LogSourceResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LogSourceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(logSourceService.update(id, request)));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle active/inactive state of a log source")
    public ResponseEntity<ApiResponse<LogSourceResponse>> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(logSourceService.toggleActive(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a log source")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        logSourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
