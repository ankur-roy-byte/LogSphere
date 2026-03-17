package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ParsedLogEventRepository extends JpaRepository<ParsedLogEvent, Long>,
        JpaSpecificationExecutor<ParsedLogEvent> {

    Page<ParsedLogEvent> findByLevel(ParsedLogEvent.LogLevel level, Pageable pageable);

    Page<ParsedLogEvent> findByServiceName(String serviceName, Pageable pageable);

    List<ParsedLogEvent> findByTraceId(String traceId);

    long countByLevel(ParsedLogEvent.LogLevel level);

    long countByTimestampBetween(Instant start, Instant end);

    long countByLevelAndTimestampBetween(ParsedLogEvent.LogLevel level, Instant start, Instant end);

    @Query("SELECT p.serviceName, COUNT(p) FROM ParsedLogEvent p " +
            "WHERE p.level = :level AND p.timestamp BETWEEN :start AND :end " +
            "GROUP BY p.serviceName ORDER BY COUNT(p) DESC")
    List<Object[]> countByServiceAndLevelInWindow(
            @Param("level") ParsedLogEvent.LogLevel level,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("SELECT p.exceptionType, COUNT(p) FROM ParsedLogEvent p " +
            "WHERE p.exceptionType IS NOT NULL AND p.timestamp BETWEEN :start AND :end " +
            "GROUP BY p.exceptionType ORDER BY COUNT(p) DESC")
    List<Object[]> findTopExceptions(
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);

    @Query("SELECT p.message, COUNT(p) FROM ParsedLogEvent p " +
            "WHERE p.timestamp BETWEEN :start AND :end " +
            "GROUP BY p.message HAVING COUNT(p) > :minCount ORDER BY COUNT(p) DESC")
    List<Object[]> findRepeatedMessages(
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("minCount") long minCount,
            Pageable pageable);
}
