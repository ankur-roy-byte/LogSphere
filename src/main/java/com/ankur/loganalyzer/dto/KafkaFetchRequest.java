package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record KafkaFetchRequest(
        @NotBlank(message = "Topic is required")
        String topic,

        Integer partition,

        Long offset,

        int limit
) {
    public KafkaFetchRequest {
        if (limit <= 0 || limit > 5000) limit = 1000;
    }
}
