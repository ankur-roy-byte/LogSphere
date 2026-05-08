package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.cache.CacheStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("CacheController Integration Tests")
class CacheControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheStats cacheStats;

    @BeforeEach
    void setUp() {
        cacheStats.reset();
    }

    @Test
    @DisplayName("Should return cache statistics")
    void testGetCacheStats() throws Exception {
        cacheStats.recordHit();
        cacheStats.recordHit();
        cacheStats.recordMiss();
        cacheStats.recordPut();

        mockMvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", equalTo(2)))
                .andExpect(jsonPath("$.misses", equalTo(1)))
                .andExpect(jsonPath("$.puts", equalTo(1)))
                .andExpect(jsonPath("$.hitRate", containsString("66.67")))
                .andExpect(jsonPath("$.totalOperations", equalTo(3)));
    }

    @Test
    @DisplayName("Should reset cache statistics")
    void testResetCacheStats() throws Exception {
        cacheStats.recordHit();
        cacheStats.recordMiss();

        mockMvc.perform(post("/api/cache/reset-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("Cache statistics reset")));

        mockMvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", equalTo(0)))
                .andExpect(jsonPath("$.misses", equalTo(0)));
    }

    @Test
    @DisplayName("Should calculate hit rate correctly")
    void testCacheHitRateCalculation() throws Exception {
        for (int i = 0; i < 7; i++) {
            cacheStats.recordHit();
        }
        for (int i = 0; i < 3; i++) {
            cacheStats.recordMiss();
        }

        mockMvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hits", equalTo(7)))
                .andExpect(jsonPath("$.misses", equalTo(3)))
                .andExpect(jsonPath("$.hitRate", containsString("70.00")));
    }
}
