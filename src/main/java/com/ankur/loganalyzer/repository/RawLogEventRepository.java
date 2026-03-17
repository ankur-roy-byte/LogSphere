package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.RawLogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RawLogEventRepository extends JpaRepository<RawLogEvent, Long> {

    List<RawLogEvent> findByTimestampBetween(Instant start, Instant end);

    List<RawLogEvent> findBySourceId(Long sourceId);

    long countByTimestampBetween(Instant start, Instant end);
}
