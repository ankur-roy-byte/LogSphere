package com.ankur.loganalyzer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Integration tests for HealthCheckController
 * Tests application health endpoints and dependency status
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthCheckControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testOverallHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()))
                .andExpect(jsonPath("$.components", notNullValue()));
    }

    @Test
    void testDatabaseHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health/database")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    void testRedisHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health/redis")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    void testLivenessProbe() throws Exception {
        mockMvc.perform(get("/api/health/live")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    void testReadinessProbe() throws Exception {
        mockMvc.perform(get("/api/health/ready")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    void testStartupProbe() throws Exception {
        mockMvc.perform(get("/api/health/startup")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }
}
