package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.dto.LogUploadRequest;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("LogController Integration Tests")
class LogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParsedLogEventRepository parsedLogEventRepository;

    @Autowired
    private RawLogEventRepository rawLogEventRepository;

    @BeforeEach
    void setUp() {
        parsedLogEventRepository.deleteAll();
        rawLogEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should upload log successfully")
    void uploadLogSuccess() throws Exception {
        LogUploadRequest request = TestDataFactory.createLogUploadRequest();

        mockMvc.perform(post("/api/logs/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLines", equalTo(1)))
                .andExpect(jsonPath("$.parsedSuccessfully", equalTo(1)))
                .andExpect(jsonPath("$.parseFailures", equalTo(0)));
    }

    @Test
    @DisplayName("Should reject invalid upload payload")
    void rejectInvalidUploadPayload() throws Exception {
        LogUploadRequest request = LogUploadRequest.builder()
                .sourceName("test-service")
                .format("invalid-format")
                .content("some content")
                .build();

        mockMvc.perform(post("/api/logs/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should search logs with pagination")
    void searchLogsWithPagination() throws Exception {
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent());

        mockMvc.perform(get("/api/logs/search")
                        .param("serviceName", "test-service")
                        .param("level", "INFO")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.number", equalTo(0)));
    }

    @Test
    @DisplayName("Should get log by ID")
    void getLogById() throws Exception {
        var saved = parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent());

        mockMvc.perform(get("/api/logs/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(saved.getId().intValue())))
                .andExpect(jsonPath("$.message", notNullValue()));
    }
}
