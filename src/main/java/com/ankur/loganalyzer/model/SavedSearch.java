package com.ankur.loganalyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "saved_searches", indexes = {
        @Index(name = "idx_saved_search_name", columnList = "name"),
        @Index(name = "idx_saved_search_created_by", columnList = "createdBy"),
        @Index(name = "idx_saved_search_service_level", columnList = "serviceName, level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedSearch extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String serviceName;

    @Column(length = 20)
    private String level;

    @Column(length = 128)
    private String traceId;

    @Column(length = 255)
    private String keyword;

    @Column(length = 100)
    private String host;

    private Instant startTime;

    private Instant endTime;

    @Column(nullable = false, length = 100)
    private String createdBy;
}
