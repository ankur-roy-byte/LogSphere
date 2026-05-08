package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationTestRequest(
        @NotNull Channel channel
) {
    public enum Channel { EMAIL, SLACK, ALL }
}
