package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
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
 * Integration tests for AlertController
 * Tests alert creation, retrieval, update, and deletion
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @BeforeEach
    void setUp() {
        alertRuleRepository.deleteAll();
    }

    @Test
    void testCreateAlertRuleSuccess() throws Exception {
        AlertRuleRequest request = new AlertRuleRequest();
        request.setName("High Error Rate");
        request.setDescription("Alert when error rate exceeds 10%");
        request.setServiceName("api-service");
        request.setCondition("ERROR");
        request.setThreshold(10L);
        request.setEnabled(true);

        mockMvc.perform(post("/api/alerts/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("High Error Rate")));
    }

    @Test
    void testCreateAlertRuleWithInvalidData() throws Exception {
        AlertRuleRequest request = new AlertRuleRequest();
        request.setName("");  // Empty name
        request.setDescription("Invalid rule");
        request.setServiceName("api-service");
        request.setCondition("ERROR");
        request.setThreshold(10L);

        mockMvc.perform(post("/api/alerts/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllAlertRules() throws Exception {
        // Create sample rules
        for (int i = 0; i < 5; i++) {
            AlertRule rule = new AlertRule();
            rule.setName("Alert " + i);
            rule.setServiceName("service-" + i);
            rule.setCondition("ERROR");
            rule.setThreshold(10L);
            rule.setEnabled(true);
            alertRuleRepository.save(rule);
        }

        mockMvc.perform(get("/api/alerts/rules")
                .param("limit", "20")
                .param("offset", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    @Test
    void testGetAlertRuleById() throws Exception {
        AlertRule rule = new AlertRule();
        rule.setName("Test Alert");
        rule.setServiceName("test-service");
        rule.setCondition("WARN");
        rule.setThreshold(5L);
        rule.setEnabled(true);
        AlertRule saved = alertRuleRepository.save(rule);

        mockMvc.perform(get("/api/alerts/rules/{id}", saved.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Test Alert")));
    }

    @Test
    void testUpdateAlertRule() throws Exception {
        AlertRule rule = new AlertRule();
        rule.setName("Original Name");
        rule.setServiceName("test-service");
        rule.setCondition("ERROR");
        rule.setThreshold(10L);
        rule.setEnabled(true);
        AlertRule saved = alertRuleRepository.save(rule);

        AlertRuleRequest updateRequest = new AlertRuleRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setDescription("Updated description");
        updateRequest.setServiceName("test-service");
        updateRequest.setCondition("WARN");
        updateRequest.setThreshold(5L);
        updateRequest.setEnabled(false);

        mockMvc.perform(put("/api/alerts/rules/{id}", saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Updated Name")));
    }

    @Test
    void testDeleteAlertRule() throws Exception {
        AlertRule rule = new AlertRule();
        rule.setName("To Delete");
        rule.setServiceName("test-service");
        rule.setCondition("ERROR");
        rule.setThreshold(10L);
        rule.setEnabled(true);
        AlertRule saved = alertRuleRepository.save(rule);

        mockMvc.perform(delete("/api/alerts/rules/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify deletion
        mockMvc.perform(get("/api/alerts/rules/{id}", saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAlertsByService() throws Exception {
        // Create rules for different services
        for (String service : new String[]{"api-service", "auth-service", "db-service"}) {
            AlertRule rule = new AlertRule();
            rule.setName("Alert for " + service);
            rule.setServiceName(service);
            rule.setCondition("ERROR");
            rule.setThreshold(10L);
            rule.setEnabled(true);
            alertRuleRepository.save(rule);
        }

        mockMvc.perform(get("/api/alerts/by-service")
                .param("serviceName", "api-service")
                .param("limit", "20")
                .param("offset", "0")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].serviceName", is("api-service")));
    }
}
