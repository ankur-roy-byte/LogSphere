package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkRuleRequest(
        @NotEmpty List<Long> ids
) {}
