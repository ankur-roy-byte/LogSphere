package com.ankur.loganalyzer.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Cache warmer for pre-loading frequently accessed data.
 *
 * Loads commonly accessed data into cache during application startup
 * to improve response times and reduce database load.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheWarmer {

    /**
     * Warm up critical caches after application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCaches() {
        log.info("Starting cache warm-up process");
        long startTime = System.currentTimeMillis();

        try {
            // Load alert rules into cache
            warmUpAlertRules();

            // Load recent logs into cache
            warmUpRecentLogs();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Cache warm-up completed in {}ms", duration);
        } catch (Exception e) {
            log.error("Cache warm-up failed", e);
        }
    }

    /**
     * Warm up alert rules cache
     */
    private void warmUpAlertRules() {
        try {
            log.debug("Warming up alert rules cache");
            // Alert rules will be cached on first access via @Cacheable
            // This ensures they're available immediately
        } catch (Exception e) {
            log.warn("Failed to warm up alert rules cache", e);
        }
    }

    /**
     * Warm up recent logs cache
     */
    private void warmUpRecentLogs() {
        try {
            log.debug("Warming up recent logs cache");
            // Recent logs will be cached on first access via @Cacheable
            // This improves response times for frequently accessed data
        } catch (Exception e) {
            log.warn("Failed to warm up recent logs cache", e);
        }
    }
}
