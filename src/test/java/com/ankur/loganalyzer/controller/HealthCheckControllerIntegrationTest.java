package com.ankur.loganalyzer.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("HealthCheckController Integration Tests")
class HealthCheckControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return overall health status")
    void testGetOverallHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().is(anyOf(equalTo(200), equalTo(503))))
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    @DisplayName("Should return database health status")
    void testGetDatabaseHealth() throws Exception {
        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    @DisplayName("Should return Redis health status")
    void testGetRedisHealth() throws Exception {
        mockMvc.perform(get("/api/health/redis"))
                .andExpect(status().is(anyOf(equalTo(200), equalTo(503))))
                .andExpect(jsonPath("$.status", notNullValue()));
    }

    @Test
    @DisplayName("Should support liveness probe")
    void testLivenessProbe() throws Exception {
        mockMvc.perform(get("/api/health/live"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should support readiness probe")
    void testReadinessProbe() throws Exception {
        mockMvc.perform(get("/api/health/ready"))
                .andExpect(status().is(anyOf(equalTo(200), equalTo(503))));
    }

    @Test
    @DisplayName("Should support startup probe")
    void testStartupProbe() throws Exception {
        mockMvc.perform(get("/api/health/startup"))
                .andExpect(status().isOk());
    }
}
