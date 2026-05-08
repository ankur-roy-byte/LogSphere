package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.AlertStatsResponse;
import com.ankur.loganalyzer.model.AlertEvent;
import com.ankur.loganalyzer.repository.AlertEventRepository;
import com.ankur.loganalyzer.repository.AlertRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertStatsService {

    private final AlertEventRepository alertEventRepository;
    private final AlertRuleRepository alertRuleRepository;

    public AlertStatsResponse getStats(long windowMinutes) {
        Instant end   = Instant.now();
        Instant start = end.minus(windowMinutes, ChronoUnit.MINUTES);

        List<AlertEvent> windowEvents = alertEventRepository.findByTriggeredAtBetween(start, end);

        Map<String, Long> topFiring = windowEvents.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getRule().getName(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        long totalRules   = alertRuleRepository.count();
        long enabledRules = alertRuleRepository.findByEnabledTrue().size();
        long active       = alertEventRepository.countByResolvedFalse();
        long total        = alertEventRepository.count();

        return new AlertStatsResponse(
                totalRules,
                enabledRules,
                active,
                total - active,
                topFiring,
                windowMinutes,
                start,
                end
        );
    }
}
