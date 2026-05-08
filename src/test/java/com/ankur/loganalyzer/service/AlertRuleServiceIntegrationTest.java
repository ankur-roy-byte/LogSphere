package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.repository.AlertEventRepository;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AlertService Integration Tests")
class AlertRuleServiceIntegrationTest {

    @Autowired
    private AlertService alertService;

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
    @DisplayName("Should create alert rule successfully")
    void createAlertRule() {
        AlertRuleRequest request = TestDataFactory.createAlertRuleRequest("Test Rule", "test-service");

        var rule = alertService.createRule(request);

        assertNotNull(rule);
        assertNotNull(rule.getId());
        assertEquals(request.name(), rule.getName());
        assertEquals(request.serviceName(), rule.getServiceName());
    }

    @Test
    @DisplayName("Should retrieve all alert rules")
    void getAllAlertRules() {
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 1", "service-1", 100));
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 2", "service-2", 150));

        var rules = alertService.getAllRules();

        assertNotNull(rules);
        assertTrue(rules.size() >= 2);
    }

    @Test
    @DisplayName("Should delete alert rule")
    void deleteAlertRule() {
        var rule = alertRuleRepository.save(TestDataFactory.createAlertRule());

        alertService.deleteRule(rule.getId());

        assertFalse(alertRuleRepository.existsById(rule.getId()));
    }
}
