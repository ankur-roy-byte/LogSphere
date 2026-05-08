package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggest")
@RequiredArgsConstructor
@Tag(name = "Suggestions", description = "Autocomplete helpers for search fields")
public class SuggestController {

    private final ParsedLogEventRepository parsedLogEventRepository;

    @GetMapping("/services")
    @Operation(summary = "Autocomplete service names", description = "Returns distinct service names matching the given prefix, useful for search bar dropdowns")
    public ResponseEntity<ApiResponse<List<String>>> suggestServices(
            @RequestParam(defaultValue = "") String prefix,
            @RequestParam(defaultValue = "10") int limit) {

        List<String> suggestions = parsedLogEventRepository
                .findDistinctServiceNamesByPrefix(prefix, PageRequest.of(0, Math.min(limit, 50)));
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
