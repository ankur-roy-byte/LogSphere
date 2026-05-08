package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.ExceptionTrendResponse;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExceptionTrendService {

    private final ParsedLogEventRepository parsedLogEventRepository;

    public ExceptionTrendResponse getTrend(long windowMinutes, int limit) {
        Instant end   = Instant.now();
        Instant start = end.minus(windowMinutes, ChronoUnit.MINUTES);

        List<Object[]> rows = parsedLogEventRepository.findTopExceptions(
                start, end, PageRequest.of(0, Math.min(limit, 100)));

        List<ExceptionTrendResponse.Entry> entries = rows.stream()
                .map(row -> new ExceptionTrendResponse.Entry((String) row[0], (Long) row[1]))
                .toList();

        return new ExceptionTrendResponse(entries, windowMinutes, start, end);
    }
}
