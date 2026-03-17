package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.AlertEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AlertEventRepository extends JpaRepository<AlertEvent, Long> {

    Page<AlertEvent> findByResolvedFalseOrderByTriggeredAtDesc(Pageable pageable);

    List<AlertEvent> findByRuleIdAndResolvedFalse(Long ruleId);

    List<AlertEvent> findByTriggeredAtBetween(Instant start, Instant end);

    long countByResolvedFalse();
}
