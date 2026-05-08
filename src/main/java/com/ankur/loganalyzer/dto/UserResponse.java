package com.ankur.loganalyzer.dto;

import com.ankur.loganalyzer.model.AppUser;
import com.ankur.loganalyzer.model.UserRole;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        UserRole role,
        boolean enabled,
        Instant createdAt
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
