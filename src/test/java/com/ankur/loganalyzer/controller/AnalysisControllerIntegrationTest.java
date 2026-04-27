package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.model.AnalysisResult;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AnalysisController
 * Tests analysis result retrieval and filtering
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalysisControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @BeforeEach
    void setUp() {
        analysisResultRepository.deleteAll();
    }

    @Test
    void testGetAnalysisResults() throws Exception {
        // Create sample analysis results
        for (int i = 0; i < 5; i++) {
            AnalysisResult result = new AnalysisResult();
            result.setServiceName("service-" + i);
            result.setTotalLogs(100L + i);
            result.setErrorCount(10L + i);
            result.setWarningCount(20L + i);
            result.setAnomalyCount(5L);
            analysisResultRepository.save(result);
        }

        mockMvc.perform(get("/api/analysis")
                .param("limit", "20")
                .param("offset", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    @Test
    void testGetAnalysisResultById() throws Exception {
        AnalysisResult result = new AnalysisResult();
        result.setServiceName("test-service");
        result.setTotalLogs(100L);
        result.setErrorCount(10L);
        result.setWarningCount(20L);
        result.setAnomalyCount(2L);
        AnalysisResult saved = analysisResultRepository.save(result);

        mockMvc.perform(get("/api/analysis/{id}", saved.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.serviceName", is("test-service")));
    }

    @Test
    void testGetAnalysisResultByService() throws Exception {
        // Create results for different services
        AnalysisResult result1 = new AnalysisResult();
        result1.setServiceName("api-service");
        result1.setTotalLogs(150L);
        result1.setErrorCount(15L);
        result1.setWarningCount(30L);
        result1.setAnomalyCount(3L);
        analysisResultRepository.save(result1);

        AnalysisResult result2 = new AnalysisResult();
        result2.setServiceName("auth-service");
        result2.setTotalLogs(200L);
        result2.setErrorCount(5L);
        result2.setWarningCount(25L);
        result2.setAnomalyCount(1L);
        analysisResultRepository.save(result2);

        mockMvc.perform(get("/api/analysis/by-service")
                .param("serviceName", "api-service")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.serviceName", is("api-service")))
                .andExpect(jsonPath("$.data.totalLogs", is(150)));
    }

    @Test
    void testGetAnalysisStatistics() throws Exception {
        // Create multiple analysis results
        for (int i = 0; i < 3; i++) {
            AnalysisResult result = new AnalysisResult();
            result.setServiceName("service-" + i);
            result.setTotalLogs(100L * (i + 1));
            result.setErrorCount(10L * (i + 1));
            result.setWarningCount(20L * (i + 1));
            result.setAnomalyCount(5L);
            analysisResultRepository.save(result);
        }

        mockMvc.perform(get("/api/analysis/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    void testTriggerAnalysis() throws Exception {
        mockMvc.perform(post("/api/analysis/trigger")
                .param("serviceName", "test-service")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Analysis")));
    }
}
