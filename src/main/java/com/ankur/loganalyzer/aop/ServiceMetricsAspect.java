package com.ankur.loganalyzer.aop;

import com.ankur.loganalyzer.annotation.MetricCategory;
import com.ankur.loganalyzer.annotation.Tracked;
import com.ankur.loganalyzer.metrics.MetricsCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that intercepts methods annotated with @Tracked and records
 * timing + success/failure data into MetricsCollectorService automatically.
 *
 * Eliminates manual metrics calls scattered across service classes.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceMetricsAspect {

    private final MetricsCollectorService metricsCollectorService;

    @Around("@annotation(tracked)")
    public Object trackServiceMethod(ProceedingJoinPoint pjp, Tracked tracked) throws Throwable {
        long start = System.currentTimeMillis();
        boolean success = true;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            success = false;
            throw t;
        } finally {
            long duration = System.currentTimeMillis() - start;
            recordMetric(tracked.category(), duration, success, pjp.getSignature().toShortString());
        }
    }

    private void recordMetric(MetricCategory category, long duration, boolean success, String signature) {
        try {
            switch (category) {
                case INGESTION -> metricsCollectorService.recordLogIngestion(duration, success);
                case PARSING   -> metricsCollectorService.recordLogParsing(duration, success);
                case ANALYSIS  -> metricsCollectorService.recordAnalysisExecution(duration, success);
                case ALERT     -> metricsCollectorService.recordAlertTriggered(success);
                case API       -> metricsCollectorService.recordApiRequest(duration, success);
            }
            log.debug("Metrics [{}] {}: {}ms success={}", category, signature, duration, success);
        } catch (Exception e) {
            log.warn("Failed to record metrics for {}", signature, e);
        }
    }
}
