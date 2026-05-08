package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record KafkaFetchRequest(
        @NotBlank String bootstrapServers,
        @NotBlank String topic,
        String groupId,
        @Min(1) Integer limit
) {

    public int resolvedLimit() {
        return limit != null ? limit : 500;
    }
}
