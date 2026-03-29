package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.TestDataFactory;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AlertRuleService Integration Tests")
class AlertRuleServiceIntegrationTest {

    @Autowired
    private AlertRuleService alertRuleService;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @BeforeEach
    void setUp() {
        alertRuleRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create alert rule successfully")
    void testCreateAlertRule() {
        AlertRuleRequest request = TestDataFactory.createAlertRuleRequest("Test Rule", "test-service");

        var rule = alertRuleService.createAlertRule(request);

        assertNotNull(rule);
        assertNotNull(rule.getId());
        assertEquals(request.getName(), rule.getName());
        assertEquals(request.getServiceName(), rule.getServiceName());
    }

    @Test
    @DisplayName("Should retrieve all alert rules")
    void testGetAllAlertRules() {
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 1", "service-1", 100));
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 2", "service-2", 150));

        var rules = alertRuleService.getAllAlertRules();

        assertNotNull(rules);
        assertTrue(rules.size() >= 2);
    }

    @Test
    @DisplayName("Should get alert rules by service")
    void testGetAlertRulesByService() {
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 1", "service-1", 100));
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 2", "service-1", 150));
        alertRuleRepository.save(TestDataFactory.createAlertRule("Rule 3", "service-2", 200));

        var rules = alertRuleService.getAlertRulesByService("service-1");

        assertNotNull(rules);
        assertEquals(2, rules.size());
    }

    @Test
    @DisplayName("Should update alert rule")
    void testUpdateAlertRule() {
        var rule = alertRuleRepository.save(TestDataFactory.createAlertRule("Original", "service-1", 100));
        AlertRuleRequest updateRequest = TestDataFactory.createAlertRuleRequest("Updated", "service-1");

        var updated = alertRuleService.updateAlertRule(rule.getId(), updateRequest);

        assertNotNull(updated);
        assertEquals("Updated", updated.getName());
    }

    @Test
    @DisplayName("Should delete alert rule")
    void testDeleteAlertRule() {
        var rule = alertRuleRepository.save(TestDataFactory.createAlertRule());

        alertRuleService.deleteAlertRule(rule.getId());

        var deleted = alertRuleRepository.findById(rule.getId());
        assertTrue(deleted.isEmpty());
    }

    @Test
    @DisplayName("Should enable alert rule")
    void testEnableAlertRule() {
        var rule = alertRuleRepository.save(TestDataFactory.createAlertRule());
        rule.setEnabled(false);
        alertRuleRepository.save(rule);

        alertRuleService.enableAlertRule(rule.getId());

        var enabled = alertRuleRepository.findById(rule.getId()).orElse(null);
        assertNotNull(enabled);
        assertTrue(enabled.isEnabled());
    }
}
