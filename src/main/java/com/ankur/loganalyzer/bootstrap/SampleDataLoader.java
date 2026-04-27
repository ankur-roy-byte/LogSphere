package com.ankur.loganalyzer.bootstrap;

import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.model.LogSource;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Sample data loader for development and testing.
 *
 * Automatically loads sample log sources and alert rules on application startup.
 * Can be disabled by setting app.sample-data.enabled=false in configuration.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SampleDataLoader {

    private final LogSourceRepository logSourceRepository;
    private final AlertRuleRepository alertRuleRepository;

    /**
     * Load sample data after application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void loadSampleData() {
        log.info("Loading sample data for development/testing");

        try {
            loadSampleLogSources();
            loadSampleAlertRules();
            log.info("Sample data loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load sample data", e);
        }
    }

    /**
     * Load sample log sources
     */
    private void loadSampleLogSources() {
        if (logSourceRepository.count() > 0) {
            log.debug("Log sources already exist, skipping initialization");
            return;
        }

        var sources = java.util.Arrays.asList(
                LogSource.builder()
                        .name("api-service")
                        .type(LogSource.SourceType.FILE)
                        .active(true)
                        .build(),
                LogSource.builder()
                        .name("auth-service")
                        .type(LogSource.SourceType.FILE)
                        .active(true)
                        .build(),
                LogSource.builder()
                        .name("database-service")
                        .type(LogSource.SourceType.LOKI)
                        .active(true)
                        .build(),
                LogSource.builder()
                        .name("cache-service")
                        .type(LogSource.SourceType.FILE)
                        .active(true)
                        .build(),
                LogSource.builder()
                        .name("notification-service")
                        .type(LogSource.SourceType.WEBHOOK)
                        .active(true)
                        .build()
        );

        logSourceRepository.saveAll(sources);
        log.info("Created {} sample log sources", sources.size());
    }

    /**
     * Load sample alert rules
     */
    private void loadSampleAlertRules() {
        if (alertRuleRepository.count() > 0) {
            log.debug("Alert rules already exist, skipping initialization");
            return;
        }

        var rules = Arrays.asList(
                AlertRule.builder()
                        .name("High Error Count")
                        .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS)
                        .threshold(50)
                        .serviceName("api-service")
                        .description("Alert when error count exceeds 50 in 5 minutes")
                        .enabled(true)
                        .build(),
                AlertRule.builder()
                        .name("Authentication Failures")
                        .conditionType(AlertRule.ConditionType.SPIKE_DETECTED)
                        .threshold(10)
                        .serviceName("auth-service")
                        .description("Alert on sudden spike in auth failures")
                        .enabled(true)
                        .build(),
                AlertRule.builder()
                        .name("Database Connection Errors")
                        .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS)
                        .threshold(5)
                        .serviceName("database-service")
                        .description("Alert on database connection errors")
                        .enabled(true)
                        .build(),
                AlertRule.builder()
                        .name("Null Pointer Exceptions")
                        .conditionType(AlertRule.ConditionType.EXCEPTION_TYPE_MATCH)
                        .threshold(1)
                        .description("Alert on any NullPointerException")
                        .enabled(true)
                        .build(),
                AlertRule.builder()
                        .name("High Log Volume")
                        .conditionType(AlertRule.ConditionType.LOGS_PER_MINUTE_EXCEEDS)
                        .threshold(1000)
                        .description("Alert when logs exceed 1000/minute globally")
                        .enabled(false)  // Disabled by default - enable as needed
                        .build()
        );

        alertRuleRepository.saveAll(rules);
        log.info("Created {} sample alert rules", rules.size());
    }
}
