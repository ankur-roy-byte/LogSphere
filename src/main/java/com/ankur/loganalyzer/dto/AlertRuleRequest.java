package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AlertRuleRequest(
        @NotBlank(message = "Alert rule name is required")
        @Size(min = 1, max = 100, message = "Alert rule name must be between 1 and 100 characters")
        String name,

        @NotNull(message = "Condition type is required")
        @NotBlank(message = "Condition type cannot be empty")
        String conditionType,

        @Min(value = 1, message = "Threshold must be at least 1")
        int threshold,

        @Size(max = 100, message = "Service name cannot exceed 100 characters")
        String serviceName,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description
) {
}
