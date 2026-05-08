package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Size(min = 8) String password,
        @NotNull UserRole role
) {}
