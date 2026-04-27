package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.model.AnalysisResult;
import com.ankur.loganalyzer.repository.AnalysisResultRepository;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.ankur.loganalyzer.repository.RawLogEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogRetentionServiceTest {

    @Mock private RawLogEventRepository rawLogEventRepository;
    @Mock private ParsedLogEventRepository parsedLogEventRepository;
    @Mock private AnalysisResultRepository analysisResultRepository;
    @Mock private ApplicationProperties properties;

    @InjectMocks
    private LogRetentionService retentionService;

    private ApplicationProperties.Retention retentionConfig;

    @BeforeEach
    void setUp() {
        retentionConfig = new ApplicationProperties.Retention();
        retentionConfig.setEnabled(true);
        retentionConfig.setParsedLogRetentionDays(30);
        retentionConfig.setRawLogRetentionDays(7);
        retentionConfig.setAnalysisResultRetentionDays(90);
        when(properties.getRetention()).thenReturn(retentionConfig);
    }

    @Test
    void purgeOldLogs_whenEnabled_callsDeleteOnAllRepositories() {
        when(parsedLogEventRepository.deleteByTimestampBefore(any())).thenReturn(5L);
        when(rawLogEventRepository.deleteByTimestampBefore(any())).thenReturn(3L);
        when(analysisResultRepository.findAll()).thenReturn(List.of());

        LogRetentionService.RetentionResult result = retentionService.purgeOldLogs();

        verify(parsedLogEventRepository).deleteByTimestampBefore(any(Instant.class));
        verify(rawLogEventRepository).deleteByTimestampBefore(any(Instant.class));
        verify(analysisResultRepository).findAll();
        assertThat(result.executed()).isTrue();
    }

    @Test
    void purgeOldLogs_whenDisabled_skipsAllDeletesAndReturnsSkipped() {
        retentionConfig.setEnabled(false);

        LogRetentionService.RetentionResult result = retentionService.purgeOldLogs();

        verifyNoInteractions(parsedLogEventRepository);
        verifyNoInteractions(rawLogEventRepository);
        verifyNoInteractions(analysisResultRepository);
        assertThat(result.executed()).isFalse();
        assertThat(result.parsedLogsDeleted()).isZero();
        assertThat(result.rawLogsDeleted()).isZero();
    }

    @Test
    void purgeOldLogs_returnsDeleteCountsFromRepositories() {
        when(parsedLogEventRepository.deleteByTimestampBefore(any())).thenReturn(12L);
        when(rawLogEventRepository.deleteByTimestampBefore(any())).thenReturn(8L);
        when(analysisResultRepository.findAll()).thenReturn(List.of());

        LogRetentionService.RetentionResult result = retentionService.purgeOldLogs();

        assertThat(result.parsedLogsDeleted()).isEqualTo(12L);
        assertThat(result.rawLogsDeleted()).isEqualTo(8L);
        assertThat(result.analysisResultsDeleted()).isZero();
    }

    @Test
    void purgeOldLogs_computesCutoffDatesFromRetentionDays() {
        retentionConfig.setParsedLogRetentionDays(30);
        retentionConfig.setRawLogRetentionDays(7);
        when(parsedLogEventRepository.deleteByTimestampBefore(any())).thenReturn(0L);
        when(rawLogEventRepository.deleteByTimestampBefore(any())).thenReturn(0L);
        when(analysisResultRepository.findAll()).thenReturn(List.of());

        Instant before = Instant.now();
        retentionService.purgeOldLogs();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> parsedCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> rawCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(parsedLogEventRepository).deleteByTimestampBefore(parsedCaptor.capture());
        verify(rawLogEventRepository).deleteByTimestampBefore(rawCaptor.capture());

        Instant expectedParsedCutoff = before.minus(30, ChronoUnit.DAYS);
        Instant expectedRawCutoff = before.minus(7, ChronoUnit.DAYS);

        assertThat(parsedCaptor.getValue())
                .isCloseTo(expectedParsedCutoff, within(5, ChronoUnit.SECONDS));
        assertThat(rawCaptor.getValue())
                .isCloseTo(expectedRawCutoff, within(5, ChronoUnit.SECONDS));
    }

    @Test
    void purgeOldLogs_deletesOldAnalysisResultsOlderThanCutoff() {
        AnalysisResult old = new AnalysisResult();
        old.setGeneratedAt(Instant.now().minus(100, ChronoUnit.DAYS));

        AnalysisResult recent = new AnalysisResult();
        recent.setGeneratedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        when(parsedLogEventRepository.deleteByTimestampBefore(any())).thenReturn(0L);
        when(rawLogEventRepository.deleteByTimestampBefore(any())).thenReturn(0L);
        when(analysisResultRepository.findAll()).thenReturn(List.of(old, recent));

        LogRetentionService.RetentionResult result = retentionService.purgeOldLogs();

        ArgumentCaptor<List<AnalysisResult>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(analysisResultRepository).deleteAll(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).hasSize(1).containsExactly(old);
        assertThat(result.analysisResultsDeleted()).isEqualTo(1L);
    }

    @Test
    void getStatus_returnsCountsFromRepositoriesAndCurrentConfig() {
        when(parsedLogEventRepository.count()).thenReturn(500L);
        when(rawLogEventRepository.count()).thenReturn(200L);
        when(analysisResultRepository.count()).thenReturn(50L);

        LogRetentionService.RetentionStatus status = retentionService.getStatus();

        assertThat(status.totalParsedLogs()).isEqualTo(500L);
        assertThat(status.totalRawLogs()).isEqualTo(200L);
        assertThat(status.totalAnalysisResults()).isEqualTo(50L);
        assertThat(status.enabled()).isTrue();
        assertThat(status.parsedLogRetentionDays()).isEqualTo(30);
        assertThat(status.rawLogRetentionDays()).isEqualTo(7);
        assertThat(status.analysisResultRetentionDays()).isEqualTo(90);
        assertThat(status.checkedAt()).isNotNull();
    }

    @Test
    void purgeOldLogs_whenAnalysisRepoFails_stillReturnsResultWithZeroAnalysisCount() {
        when(parsedLogEventRepository.deleteByTimestampBefore(any())).thenReturn(4L);
        when(rawLogEventRepository.deleteByTimestampBefore(any())).thenReturn(2L);
        when(analysisResultRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        LogRetentionService.RetentionResult result = retentionService.purgeOldLogs();

        assertThat(result.executed()).isTrue();
        assertThat(result.parsedLogsDeleted()).isEqualTo(4L);
        assertThat(result.rawLogsDeleted()).isEqualTo(2L);
        assertThat(result.analysisResultsDeleted()).isZero();
    }
}
