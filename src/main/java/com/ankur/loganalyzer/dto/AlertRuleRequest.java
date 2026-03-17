package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AlertRuleRequest(
        @NotBlank(message = "Alert rule name is required")
        String name,

        @NotNull(message = "Condition type is required")
        String conditionType,

        @Min(value = 1, message = "Threshold must be at least 1")
        int threshold,

        String serviceName,

        String description
) {
}
