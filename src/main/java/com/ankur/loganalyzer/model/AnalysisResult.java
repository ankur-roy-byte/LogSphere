package com.ankur.loganalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "analysis_results", indexes = {
        @Index(name = "idx_analysis_type", columnList = "analysisType"),
        @Index(name = "idx_analysis_window", columnList = "windowStart,windowEnd")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisType analysisType;

    @Column(nullable = false)
    private String resultKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultValue;

    @Column(nullable = false)
    private Instant windowStart;

    @Column(nullable = false)
    private Instant windowEnd;

    @Column(nullable = false)
    private Instant generatedAt;

    public enum AnalysisType {
        ERROR_COUNT, WARNING_COUNT, TOP_EXCEPTIONS, SPIKE_DETECTION,
        LOGS_PER_MINUTE, SERVICE_ERROR_SUMMARY, REPEATED_MESSAGES, PATTERN_MATCH
    }
}
