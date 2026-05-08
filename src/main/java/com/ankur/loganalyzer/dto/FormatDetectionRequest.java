package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FormatDetectionRequest(
        @NotBlank @Size(max = 5000) String sample
) {}
