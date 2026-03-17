package com.ankur.loganalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "log_sources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogSource extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType type;

    @Column(columnDefinition = "TEXT")
    private String configJson;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public enum SourceType {
        LOKI, FILE, KAFKA, WEBHOOK
    }
}
