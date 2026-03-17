package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    List<AnalysisResult> findByAnalysisType(AnalysisResult.AnalysisType analysisType);

    List<AnalysisResult> findByAnalysisTypeAndWindowStartAfter(
            AnalysisResult.AnalysisType analysisType, Instant windowStart);

    List<AnalysisResult> findByWindowStartBetween(Instant start, Instant end);

    void deleteByGeneratedAtBefore(Instant before);
}
