package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.annotation.MetricCategory;
import com.ankur.loganalyzer.annotation.Tracked;
import com.ankur.loganalyzer.dto.AlertEventResponse;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.dto.SpikeDetectionResponse;
import com.ankur.loganalyzer.exception.ResourceNotFoundException;
import com.ankur.loganalyzer.model.AlertEvent;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.AlertEventRepository;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AlertNotificationService alertNotificationService;
    @Lazy
    private final LogAnalysisService logAnalysisService;

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

    @Transactional
    public AlertEventResponse resolveAlertEvent(Long id) {
        AlertEvent event = alertEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AlertEvent", id));
        if (!event.isResolved()) {
            event.setResolved(true);
            event.setResolvedAt(Instant.now());
            event = alertEventRepository.save(event);
            log.info("Resolved alert event {}", id);
        }
        return toResponse(event);
    }

    @Tracked(category = MetricCategory.ALERT, operation = "evaluate")
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
            case ERROR_COUNT_EXCEEDS -> evaluateErrorCountExceeds(rule, windowStart, windowEnd);
            case LOGS_PER_MINUTE_EXCEEDS -> evaluateLogsPerMinuteExceeds(rule, windowStart, windowEnd);
            case SPIKE_DETECTED -> evaluateSpikeDetected(rule);
            case EXCEPTION_TYPE_MATCH -> evaluateExceptionTypeMatch(rule, windowStart, windowEnd);
            case REPEATED_MESSAGE_THRESHOLD -> evaluateRepeatedMessageThreshold(rule, windowStart, windowEnd);
            default -> log.debug("Rule type {} not yet implemented", rule.getConditionType());
        }
    }

    private void evaluateErrorCountExceeds(AlertRule rule, Instant windowStart, Instant windowEnd) {
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

    private void evaluateLogsPerMinuteExceeds(AlertRule rule, Instant windowStart, Instant windowEnd) {
        long totalLogs = parsedLogEventRepository.countByTimestampBetween(windowStart, windowEnd);
        long logsPerMinute = totalLogs / 60;
        if (logsPerMinute > rule.getThreshold()) {
            triggerAlert(rule, String.format("Logs per minute %d exceeds threshold %d", logsPerMinute, rule.getThreshold()));
        }
    }

    private void evaluateSpikeDetected(AlertRule rule) {
        SpikeDetectionResponse spikes = logAnalysisService.detectSpikes();

        for (SpikeDetectionResponse.Spike spike : spikes.spikes()) {
            // If rule has a service filter, only alert for that service
            if (rule.getServiceName() != null && !rule.getServiceName().equals(spike.serviceName())) {
                continue;
            }

            // Alert if the change percentage exceeds the threshold
            if (spike.changePercentage() > rule.getThreshold()) {
                triggerAlert(rule, String.format("Spike detected for service '%s': %.1f%% increase (from %d to %d errors)",
                        spike.serviceName(), spike.changePercentage(), spike.previousCount(), spike.currentCount()));
            }
        }
    }

    private void evaluateExceptionTypeMatch(AlertRule rule, Instant windowStart, Instant windowEnd) {
        // Use description as the exception type pattern to match
        String exceptionPattern = rule.getDescription();
        if (exceptionPattern == null || exceptionPattern.isBlank()) {
            log.warn("EXCEPTION_TYPE_MATCH rule {} has no exception pattern in description", rule.getName());
            return;
        }

        List<Object[]> topExceptions = parsedLogEventRepository.findTopExceptions(
                windowStart, windowEnd, PageRequest.of(0, 50));

        for (Object[] row : topExceptions) {
            String exceptionType = row[0].toString();
            long count = (Long) row[1];

            // Check if exception type matches the pattern and count exceeds threshold
            if (exceptionType.toLowerCase().contains(exceptionPattern.toLowerCase()) && count > rule.getThreshold()) {
                triggerAlert(rule, String.format("Exception '%s' matched pattern '%s' with count %d (threshold: %d)",
                        exceptionType, exceptionPattern, count, rule.getThreshold()));
            }
        }
    }

    private void evaluateRepeatedMessageThreshold(AlertRule rule, Instant windowStart, Instant windowEnd) {
        List<Object[]> repeatedMessages = parsedLogEventRepository.findRepeatedMessages(
                windowStart, windowEnd, rule.getThreshold(), PageRequest.of(0, 10));

        if (!repeatedMessages.isEmpty()) {
            StringBuilder alertMessage = new StringBuilder("Repeated messages detected:\n");
            for (Object[] row : repeatedMessages) {
                String message = row[0].toString();
                long count = (Long) row[1];

                String truncatedMessage = message.length() > 100 ? message.substring(0, 100) + "..." : message;
                alertMessage.append(String.format("- '%s' occurred %d times\n", truncatedMessage, count));
            }
            triggerAlert(rule, alertMessage.toString());
        }
    }

    @Transactional
    public int bulkSetEnabled(List<Long> ids, boolean enabled) {
        List<AlertRule> rules = alertRuleRepository.findAllById(ids);
        rules.forEach(r -> r.setEnabled(enabled));
        alertRuleRepository.saveAll(rules);
        return rules.size();
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
        alertNotificationService.notifyAlert(event);
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
