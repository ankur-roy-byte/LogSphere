package com.ankur.loganalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "raw_log_events", indexes = {
        @Index(name = "idx_raw_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_raw_log_source", columnList = "source_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawLogEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private LogSource source;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawMessage;

    @Column(nullable = false)
    private Instant timestamp;

    private String traceId;

    private String host;

    @Column(nullable = false)
    private Instant ingestionTime;
}
