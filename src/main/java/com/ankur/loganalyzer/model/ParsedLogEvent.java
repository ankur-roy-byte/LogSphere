package com.ankur.loganalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "parsed_log_events", indexes = {
        @Index(name = "idx_parsed_log_timestamp", columnList = "timestamp"),
        @Index(name = "idx_parsed_log_service", columnList = "serviceName"),
        @Index(name = "idx_parsed_log_level", columnList = "level"),
        @Index(name = "idx_parsed_log_trace_id", columnList = "traceId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedLogEvent extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_event_id")
    private RawLogEvent rawEvent;

    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogLevel level;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String exceptionType;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false)
    private Instant timestamp;

    private String traceId;

    private String host;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> metadata;

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR, FATAL
    }
}
