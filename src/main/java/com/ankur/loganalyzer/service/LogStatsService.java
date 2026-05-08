package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.LogStatsResponse;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogStatsService {

    private final ParsedLogEventRepository parsedLogEventRepository;

    public LogStatsResponse getStats(long windowMinutes) {
        Instant end   = Instant.now();
        Instant start = end.minus(windowMinutes, ChronoUnit.MINUTES);

        Map<String, Long> countByLevel = Arrays.stream(ParsedLogEvent.LogLevel.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        level -> parsedLogEventRepository.countByLevelAndTimestampBetween(level, start, end),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<String, Long> topServicesByError = toMap(
                parsedLogEventRepository.countByServiceAndLevelInWindow(
                        ParsedLogEvent.LogLevel.ERROR, start, end));

        Map<String, Long> topServicesByVolume = toMap(
                parsedLogEventRepository.countByServiceInWindow(start, end));

        return new LogStatsResponse(
                parsedLogEventRepository.count(),
                countByLevel,
                topServicesByError,
                topServicesByVolume,
                windowMinutes,
                start,
                end
        );
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null) {
                result.put((String) row[0], (Long) row[1]);
            }
        }
        return result;
    }
}
