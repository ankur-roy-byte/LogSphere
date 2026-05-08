package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AnalysisController.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParsedLogEventRepository parsedLogEventRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @BeforeEach
    void setUp() {
        analysisResultRepository.deleteAll();
        parsedLogEventRepository.deleteAll();
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("api-service", ParsedLogEvent.LogLevel.ERROR, "Database connection failed"));
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("api-service", ParsedLogEvent.LogLevel.WARN, "Slow request"));
        parsedLogEventRepository.save(TestDataFactory.createParsedLogEvent("auth-service", ParsedLogEvent.LogLevel.INFO, "Login successful"));
    }

    @Test
    void getAnalysisSummary() throws Exception {
        mockMvc.perform(get("/api/analysis/summary")
                        .param("startTime", "2000-01-01T00:00:00Z")
                        .param("endTime", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogs", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalErrors", greaterThanOrEqualTo(1)));
    }

    @Test
    void getErrorsByLevel() throws Exception {
        mockMvc.perform(get("/api/analysis/errors/by-level")
                        .param("startTime", "2000-01-01T00:00:00Z")
                        .param("endTime", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].level", notNullValue()));
    }

    @Test
    void getErrorsByService() throws Exception {
        mockMvc.perform(get("/api/analysis/errors/by-service")
                        .param("startTime", "2000-01-01T00:00:00Z")
                        .param("endTime", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].serviceName", notNullValue()));
    }

    @Test
    void getAggregations() throws Exception {
        mockMvc.perform(get("/api/analysis/aggregations")
                        .param("startTime", "2000-01-01T00:00:00Z")
                        .param("endTime", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogs", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.byService.api-service", is(2)));
    }
}
