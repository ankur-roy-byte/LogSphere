package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogRetentionService {

    private final RawLogEventRepository rawLogEventRepository;
    private final ParsedLogEventRepository parsedLogEventRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final ApplicationProperties properties;

    @Scheduled(cron = "${scheduler.retention.cron:0 0 2 * * *}")
    public void runScheduledRetention() {
        log.info("Starting scheduled log retention purge");
        purgeOldLogs();
    }

    @Transactional
    public RetentionResult purgeOldLogs() {
        ApplicationProperties.Retention config = properties.getRetention();

        if (!config.isEnabled()) {
            log.info("Log retention is disabled, skipping purge");
            return RetentionResult.skipped();
        }

        Instant parsedCutoff = Instant.now().minus(config.getParsedLogRetentionDays(), ChronoUnit.DAYS);
        Instant rawCutoff = Instant.now().minus(config.getRawLogRetentionDays(), ChronoUnit.DAYS);
        Instant analysisCutoff = Instant.now().minus(config.getAnalysisResultRetentionDays(), ChronoUnit.DAYS);

        long parsedDeleted = parsedLogEventRepository.deleteByTimestampBefore(parsedCutoff);
        long rawDeleted = rawLogEventRepository.deleteByTimestampBefore(rawCutoff);
        long analysisDeleted = deleteOldAnalysisResults(analysisCutoff);

        log.info("Retention purge complete: {} parsed logs, {} raw logs, {} analysis results deleted",
                parsedDeleted, rawDeleted, analysisDeleted);

        return new RetentionResult(parsedDeleted, rawDeleted, analysisDeleted,
                parsedCutoff, rawCutoff, analysisCutoff, Instant.now(), true);
    }

    public RetentionStatus getStatus() {
        ApplicationProperties.Retention config = properties.getRetention();
        long totalParsed = parsedLogEventRepository.count();
        long totalRaw = rawLogEventRepository.count();
        long totalAnalysis = analysisResultRepository.count();

        return new RetentionStatus(
                config.isEnabled(),
                config.getParsedLogRetentionDays(),
                config.getRawLogRetentionDays(),
                config.getAnalysisResultRetentionDays(),
                totalParsed,
                totalRaw,
                totalAnalysis,
                Instant.now()
        );
    }

    private long deleteOldAnalysisResults(Instant cutoff) {
        try {
            var old = analysisResultRepository.findAll().stream()
                    .filter(r -> r.getGeneratedAt() != null && r.getGeneratedAt().isBefore(cutoff))
                    .toList();
            analysisResultRepository.deleteAll(old);
            return old.size();
        } catch (Exception e) {
            log.warn("Failed to purge old analysis results", e);
            return 0;
        }
    }

    public record RetentionResult(
            long parsedLogsDeleted,
            long rawLogsDeleted,
            long analysisResultsDeleted,
            Instant parsedCutoff,
            Instant rawCutoff,
            Instant analysisCutoff,
            Instant completedAt,
            boolean executed
    ) {
        public static RetentionResult skipped() {
            return new RetentionResult(0, 0, 0, null, null, null, Instant.now(), false);
        }
    }

    public record RetentionStatus(
            boolean enabled,
            int parsedLogRetentionDays,
            int rawLogRetentionDays,
            int analysisResultRetentionDays,
            long totalParsedLogs,
            long totalRawLogs,
            long totalAnalysisResults,
            Instant checkedAt
    ) {}
}
