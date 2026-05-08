package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenSearchService {

    private final ApplicationProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public boolean isEnabled() {
        return properties.getOpensearch().isEnabled();
    }

    public void indexLog(ParsedLogEvent event) {
        if (!isEnabled()) return;
        try {
            String url = buildUrl("/" + indexName() + "/_doc/" + event.getId());
            Map<String, Object> doc = Map.of(
                    "id", event.getId(),
                    "level", event.getLevel().name(),
                    "message", event.getMessage(),
                    "serviceName", event.getServiceName() != null ? event.getServiceName() : "",
                    "timestamp", event.getTimestamp().toString()
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.put(url, new HttpEntity<>(objectMapper.writeValueAsString(doc), headers));
        } catch (JsonProcessingException e) {
            log.warn("Failed to index log event {} to OpenSearch", event.getId(), e);
        }
    }

    public List<Map<String, Object>> search(String query, int size) {
        if (!isEnabled()) return List.of();
        try {
            String url = buildUrl("/" + indexName() + "/_search");
            String body = """
                    {"query":{"multi_match":{"query":"%s","fields":["message","serviceName"]}},"size":%d}
                    """.formatted(query.replace("\"", "\\\""), size);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(body, headers), Map.class);
            if (response == null) return List.of();
            @SuppressWarnings("unchecked")
            Map<String, Object> hits = (Map<String, Object>) response.get("hits");
            if (hits == null) return List.of();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");
            return hitList != null ? hitList : List.of();
        } catch (Exception e) {
            log.warn("OpenSearch query failed: {}", e.getMessage());
            return List.of();
        }
    }

    private String buildUrl(String path) {
        ApplicationProperties.OpenSearch cfg = properties.getOpensearch();
        return cfg.getScheme() + "://" + cfg.getHost() + ":" + cfg.getPort() + path;
    }

    private String indexName() {
        return properties.getOpensearch().getIndexPrefix() + "-logs";
    }
}
