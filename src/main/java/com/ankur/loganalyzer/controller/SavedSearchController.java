package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.LogSearchRequest;
import com.ankur.loganalyzer.dto.ParsedLogResponse;
import com.ankur.loganalyzer.dto.SavedSearchRequest;
import com.ankur.loganalyzer.dto.SavedSearchResponse;
import com.ankur.loganalyzer.service.LogSearchService;
import com.ankur.loganalyzer.service.SavedSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/saved-searches")
@RequiredArgsConstructor
@Tag(name = "Saved Searches", description = "APIs for storing and reusing log search filters")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;
    private final LogSearchService logSearchService;

    @PostMapping
    @Operation(summary = "Create saved search",
            description = "Persist a reusable set of log search filters")
    @ApiResponse(responseCode = "201", description = "Saved search created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid saved search request")
    public ResponseEntity<SavedSearchResponse> create(@Valid @RequestBody SavedSearchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSearchService.create(request));
    }

    @GetMapping
    @Operation(summary = "List saved searches",
            description = "Retrieve all saved searches ordered by most recently updated")
    @ApiResponse(responseCode = "200", description = "Saved searches returned successfully")
    public ResponseEntity<List<SavedSearchResponse>> list() {
        return ResponseEntity.ok(savedSearchService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get saved search",
            description = "Retrieve one saved search by ID")
    @ApiResponse(responseCode = "200", description = "Saved search found")
    @ApiResponse(responseCode = "404", description = "Saved search not found")
    public ResponseEntity<SavedSearchResponse> get(
            @Parameter(description = "Saved search ID") @PathVariable Long id) {
        return ResponseEntity.ok(savedSearchService.get(id));
    }

    @GetMapping("/{id}/logs")
    @Operation(summary = "Run saved search",
            description = "Execute a saved search against parsed logs")
    @ApiResponse(responseCode = "200", description = "Saved search results returned successfully")
    @ApiResponse(responseCode = "404", description = "Saved search not found")
    public ResponseEntity<Page<ParsedLogResponse>> run(
            @Parameter(description = "Saved search ID") @PathVariable Long id,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        LogSearchRequest criteria = savedSearchService.toLogSearchRequest(id, page, size);
        return ResponseEntity.ok(logSearchService.searchLogs(
                criteria.serviceName(),
                criteria.level(),
                criteria.traceId(),
                criteria.keyword(),
                criteria.host(),
                criteria.startTime(),
                criteria.endTime(),
                criteria.page(),
                criteria.size()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete saved search",
            description = "Delete a saved search by ID")
    @ApiResponse(responseCode = "204", description = "Saved search deleted successfully")
    @ApiResponse(responseCode = "404", description = "Saved search not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Saved search ID") @PathVariable Long id) {
        savedSearchService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
