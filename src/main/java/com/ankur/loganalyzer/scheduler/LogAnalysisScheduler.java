package com.ankur.loganalyzer.scheduler;

import com.ankur.loganalyzer.service.AlertService;
import com.ankur.loganalyzer.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class LogAnalysisScheduler {

    private final LogAnalysisService logAnalysisService;
    private final AlertService alertService;

    @Scheduled(fixedRateString = "${scheduler.analysis.rate:300000}") // default: every 5 minutes
    public void runPeriodicAnalysis() {
        log.info("Running scheduled log analysis...");
        try {
            Instant end = Instant.now();
            Instant start = end.minus(1, ChronoUnit.HOURS);
            logAnalysisService.generateSummary(start, end);
            log.info("Scheduled analysis complete");
        } catch (Exception e) {
            log.error("Scheduled analysis failed", e);
        }
    }

    @Scheduled(fixedRateString = "${scheduler.alert.rate:60000}") // default: every 1 minute
    public void runAlertEvaluation() {
        log.debug("Running scheduled alert evaluation...");
        try {
            alertService.evaluateRules();
        } catch (Exception e) {
            log.error("Scheduled alert evaluation failed", e);
        }
    }

    @Scheduled(fixedRateString = "${scheduler.spike.rate:600000}") // default: every 10 minutes
    public void runSpikeDetection() {
        log.info("Running scheduled spike detection...");
        try {
            logAnalysisService.detectSpikes();
            log.info("Spike detection complete");
        } catch (Exception e) {
            log.error("Spike detection failed", e);
        }
    }
}
