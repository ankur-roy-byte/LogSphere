package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.dto.LogUploadRequest;
import com.ankur.loganalyzer.repository.LogEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("LogController Integration Tests")
class LogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogEntryRepository logEntryRepository;

    @BeforeEach
    void setUp() {
        logEntryRepository.deleteAll();
    }

    @Test
    @DisplayName("Should upload log successfully")
    void testUploadLog_Success() throws Exception {
        LogUploadRequest request = TestDataFactory.createLogUploadRequest();

        mockMvc.perform(post("/api/logs")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("success")))
                .andExpect(jsonPath("$.message", containsString("processed")));
    }

    @Test
    @DisplayName("Should reject invalid log format")
    void testUploadLog_InvalidFormat() throws Exception {
        LogUploadRequest request = LogUploadRequest.builder()
                .source("test-service")
                .logFormat("invalid-format")
                .content("some content")
                .build();

        mockMvc.perform(post("/api/logs")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should search logs with pagination")
    void testSearchLogs_WithPagination() throws Exception {
        mockMvc.perform(get("/api/logs/search")
                .param("serviceName", "test-service")
                .param("logLevel", "INFO")
                .param("pageNumber", "1")
                .param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("success")))
                .andExpect(jsonPath("$.data.pageNumber", equalTo(1)));
    }

    @Test
    @DisplayName("Should delete logs for service")
    void testDeleteLogsForService() throws Exception {
        mockMvc.perform(delete("/api/logs")
                .param("serviceName", "test-service")
                .param("olderThanDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("success")));
    }
}
