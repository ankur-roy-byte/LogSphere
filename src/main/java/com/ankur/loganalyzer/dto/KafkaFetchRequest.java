package com.ankur.loganalyzer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record KafkaFetchRequest(
        @NotBlank String bootstrapServers,
        @NotBlank String topic,
        String groupId,
        @Min(1) @Builder.Default int limit = 500
) {
}
