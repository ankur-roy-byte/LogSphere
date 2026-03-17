package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LokiFetchRequest(
        @NotBlank(message = "Query is required")
        String query,

        long startNs,

        long endNs,

        int limit
) {
    public LokiFetchRequest {
        if (limit <= 0 || limit > 5000) limit = 1000;
    }
}
