package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.model.LogSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogSourceRequest(
        @NotBlank String name,
        @NotNull LogSource.SourceType type,
        String configJson
) {}
