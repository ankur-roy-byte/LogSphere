package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.cache.CacheStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Cache management endpoints for monitoring and controlling cache behavior.
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management", description = "Endpoints for monitoring and managing cache")
public class CacheController {

    private final CacheStats cacheStats;

    @GetMapping("/stats")
    @Operation(summary = "Get cache statistics",
            description = "Retrieve cache performance metrics (hits, misses, hit rate)")
    @ApiResponse(responseCode = "200", description = "Cache statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(Map.of(
                "hits", cacheStats.getHits().get(),
                "misses", cacheStats.getMisses().get(),
                "evictions", cacheStats.getEvictions().get(),
                "puts", cacheStats.getPuts().get(),
                "hitRate", String.format("%.2f%%", cacheStats.getHitRate()),
                "totalOperations", cacheStats.getTotalOperations()
        ));
    }

    @PostMapping("/reset-stats")
    @Operation(summary = "Reset cache statistics",
            description = "Reset all cache performance counters")
    @ApiResponse(responseCode = "200", description = "Cache statistics reset successfully")
    public ResponseEntity<Map<String, String>> resetCacheStats() {
        cacheStats.reset();
        return ResponseEntity.ok(Map.of("status", "Cache statistics reset"));
    }
}
