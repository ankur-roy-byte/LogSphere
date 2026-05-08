package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.LogVolumeResponse;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogVolumeService {

    private final ParsedLogEventRepository parsedLogEventRepository;

    public LogVolumeResponse getVolume(String granularity, Instant from, Instant to) {
        ChronoUnit unit = resolveUnit(granularity);
        String label = unit == ChronoUnit.DAYS ? "day" : "hour";

        List<ParsedLogEvent> events = parsedLogEventRepository.findByTimestampBetween(from, to);

        List<LogVolumeResponse.Bucket> buckets = events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getTimestamp().truncatedTo(unit),
                        TreeMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> new LogVolumeResponse.Bucket(entry.getKey(), entry.getValue()))
                .toList();

        return new LogVolumeResponse(buckets, label, from, to, events.size());
    }

    private ChronoUnit resolveUnit(String granularity) {
        if (granularity != null && granularity.equalsIgnoreCase("day")) {
            return ChronoUnit.DAYS;
        }
        return ChronoUnit.HOURS;
    }
}
