package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.LogSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LogSourceRepository extends JpaRepository<LogSource, Long> {

    Optional<LogSource> findByName(String name);

    List<LogSource> findByType(LogSource.SourceType type);

    List<LogSource> findByActiveTrue();
}
