package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.repository.AlertEventRepository;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AlertController.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertEventRepository alertEventRepository;

    @BeforeEach
    void setUp() {
        alertEventRepository.deleteAll();
        alertRuleRepository.deleteAll();
    }

    @Test
    void createAlertRuleSuccess() throws Exception {
        AlertRuleRequest request = TestDataFactory.createAlertRuleRequest("High Error Rate", "api-service");

        mockMvc.perform(post("/api/alerts/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("High Error Rate")))
                .andExpect(jsonPath("$.conditionType", is(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS.name())));
    }

    @Test
    void createAlertRuleWithInvalidData() throws Exception {
        AlertRuleRequest request = AlertRuleRequest.builder()
                .name("")
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS.name())
                .threshold(10)
                .serviceName("api-service")
                .description("Invalid rule")
                .build();

        mockMvc.perform(post("/api/alerts/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllAlertRules() throws Exception {
        for (int i = 0; i < 5; i++) {
            alertRuleRepository.save(TestDataFactory.createAlertRule("Alert " + i, "service-" + i, 10));
        }

        mockMvc.perform(get("/api/alerts/rules")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void deleteAlertRule() throws Exception {
        AlertRule saved = alertRuleRepository.save(TestDataFactory.createAlertRule("To Delete", "test-service", 10));

        mockMvc.perform(delete("/api/alerts/rules/{id}", saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/alerts/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAlertEventsPage() throws Exception {
        mockMvc.perform(get("/api/alerts")
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.number", is(0)));
    }
}
