package com.ankur.loganalyzer.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LokiClientService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public LokiClientService(
            @Value("${loki.base-url:http://localhost:3100}") String lokiBaseUrl,
            RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(lokiBaseUrl).build();
        this.objectMapper = new ObjectMapper();
    }

    public List<String> queryRange(String query, long startNs, long endNs, int limit) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/loki/api/v1/query_range")
                            .queryParam("query", query)
                            .queryParam("start", startNs)
                            .queryParam("end", endNs)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(String.class);

            return extractLogLines(response);
        } catch (Exception e) {
            log.error("Failed to query Loki: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<String> query(String query, int limit) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/loki/api/v1/query")
                            .queryParam("query", query)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(String.class);

            return extractLogLines(response);
        } catch (Exception e) {
            log.error("Failed to query Loki: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private List<String> extractLogLines(String response) {
        List<String> logLines = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("data").path("result");

            if (results.isArray()) {
                for (JsonNode stream : results) {
                    JsonNode values = stream.path("values");
                    if (values.isArray()) {
                        for (JsonNode entry : values) {
                            // Loki returns [timestamp, logline]
                            if (entry.isArray() && entry.size() >= 2) {
                                logLines.add(entry.get(1).asText());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Loki response", e);
        }
        return logLines;
    }
}
