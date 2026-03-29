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

@Entity
@Table(name = "alert_rules", indexes = {
        @Index(name = "idx_alert_rule_enabled", columnList = "enabled"),
        @Index(name = "idx_alert_rule_service", columnList = "serviceName"),
        @Index(name = "idx_alert_rule_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionType conditionType;

    @Column(nullable = false)
    private int threshold;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    private String serviceName;

    private String description;

    public enum ConditionType {
        ERROR_COUNT_EXCEEDS, SPIKE_DETECTED, EXCEPTION_TYPE_MATCH,
        REPEATED_MESSAGE_THRESHOLD, LOGS_PER_MINUTE_EXCEEDS
    }
}

