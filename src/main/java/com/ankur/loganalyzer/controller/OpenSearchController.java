package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.service.OpenSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "Full-text Search", description = "OpenSearch-backed full-text log search")
public class OpenSearchController {

    private final OpenSearchService openSearchService;

    @GetMapping
    @Operation(summary = "Full-text log search", description = "Searches indexed logs via OpenSearch. Returns empty list when OpenSearch is disabled.")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") int size) {

        if (!openSearchService.isEnabled()) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        return ResponseEntity.ok(ApiResponse.success(openSearchService.search(q, size)));
    }
}
