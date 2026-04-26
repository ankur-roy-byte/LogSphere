package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.RawLogEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface RawLogEventRepository extends JpaRepository<RawLogEvent, Long> {

    List<RawLogEvent> findByTimestampBetween(Instant start, Instant end);

    List<RawLogEvent> findBySourceId(Long sourceId);

    long countByTimestampBetween(Instant start, Instant end);

    @Modifying
    @Transactional
    @Query("DELETE FROM RawLogEvent r WHERE r.timestamp < :cutoff")
    long deleteByTimestampBefore(@Param("cutoff") Instant cutoff);
}
