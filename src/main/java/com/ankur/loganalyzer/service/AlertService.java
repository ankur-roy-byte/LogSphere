package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.AlertEventResponse;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.exception.ResourceNotFoundException;
import com.ankur.loganalyzer.model.AlertEvent;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AlertEventRepository;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertEventRepository alertEventRepository;
    private final ParsedLogEventRepository parsedLogEventRepository;

    public AlertRule createRule(AlertRuleRequest request) {
        AlertRule rule = AlertRule.builder()
                .name(request.name())
                .conditionType(AlertRule.ConditionType.valueOf(request.conditionType()))
                .threshold(request.threshold())
                .serviceName(request.serviceName())
                .description(request.description())
                .build();
        return alertRuleRepository.save(rule);
    }

    public List<AlertRule> getAllRules() {
        return alertRuleRepository.findAll();
    }

    public void deleteRule(Long id) {
        if (!alertRuleRepository.existsById(id)) {
            throw new ResourceNotFoundException("AlertRule", id);
        }
        alertRuleRepository.deleteById(id);
    }

    public Page<AlertEventResponse> getAlertEvents(int page, int size) {
        return alertEventRepository.findByResolvedFalseOrderByTriggeredAtDesc(
                PageRequest.of(page, size)).map(this::toResponse);
    }

    public void evaluateRules() {
        List<AlertRule> enabledRules = alertRuleRepository.findByEnabledTrue();
        Instant windowEnd = Instant.now();
        Instant windowStart = windowEnd.minus(1, ChronoUnit.HOURS);

        for (AlertRule rule : enabledRules) {
            try {
                evaluateRule(rule, windowStart, windowEnd);
            } catch (Exception e) {
                log.error("Failed to evaluate alert rule: {}", rule.getName(), e);
            }
        }
    }

    private void evaluateRule(AlertRule rule, Instant windowStart, Instant windowEnd) {
        switch (rule.getConditionType()) {
            case ERROR_COUNT_EXCEEDS -> {
                long errorCount;
                if (rule.getServiceName() != null) {
                    errorCount = parsedLogEventRepository.countByServiceAndLevelInWindow(
                            ParsedLogEvent.LogLevel.ERROR, windowStart, windowEnd).stream()
                            .filter(r -> rule.getServiceName().equals(r[0]))
                            .mapToLong(r -> (Long) r[1])
                            .sum();
                } else {
                    errorCount = parsedLogEventRepository.countByLevelAndTimestampBetween(
                            ParsedLogEvent.LogLevel.ERROR, windowStart, windowEnd);
                }
                if (errorCount > rule.getThreshold()) {
                    triggerAlert(rule, String.format("Error count %d exceeds threshold %d", errorCount, rule.getThreshold()));
                }
            }
            case LOGS_PER_MINUTE_EXCEEDS -> {
                long totalLogs = parsedLogEventRepository.countByTimestampBetween(windowStart, windowEnd);
                long logsPerMinute = totalLogs / 60;
                if (logsPerMinute > rule.getThreshold()) {
                    triggerAlert(rule, String.format("Logs per minute %d exceeds threshold %d", logsPerMinute, rule.getThreshold()));
                }
            }
            default -> log.debug("Rule type {} not yet implemented", rule.getConditionType());
        }
    }

    private void triggerAlert(AlertRule rule, String message) {
        // Check if there's already an unresolved alert for this rule
        List<AlertEvent> existing = alertEventRepository.findByRuleIdAndResolvedFalse(rule.getId());
        if (!existing.isEmpty()) {
            log.debug("Alert already active for rule: {}", rule.getName());
            return;
        }

        AlertEvent event = AlertEvent.builder()
                .rule(rule)
                .message(message)
                .triggeredAt(Instant.now())
                .build();
        alertEventRepository.save(event);
        log.warn("ALERT triggered: {} - {}", rule.getName(), message);
    }

    private AlertEventResponse toResponse(AlertEvent event) {
        return AlertEventResponse.builder()
                .id(event.getId())
                .ruleName(event.getRule().getName())
                .conditionType(event.getRule().getConditionType().name())
                .message(event.getMessage())
                .triggeredAt(event.getTriggeredAt())
                .resolved(event.isResolved())
                .resolvedAt(event.getResolvedAt())
                .build();
    }
}
