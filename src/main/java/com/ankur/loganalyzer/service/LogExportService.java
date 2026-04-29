package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogExportService {

    private static final int MAX_EXPORT_ROWS = 10_000;

    private final ParsedLogEventRepository parsedLogEventRepository;
    private final ObjectMapper objectMapper;

    public byte[] exportAsCsv(ParsedLogEvent.LogLevel level, String serviceName, Instant from, Instant to) {
        List<ParsedLogEvent> events = fetchEvents(level, from, to);

        StringBuilder csv = new StringBuilder();
        csv.append("id,timestamp,level,serviceName,message,exceptionType,host,traceId\n");

        for (ParsedLogEvent e : events) {
            if (serviceName != null && !serviceName.isBlank() &&
                    !serviceName.equalsIgnoreCase(e.getServiceName())) {
                continue;
            }
            csv.append(e.getId()).append(',')
               .append(e.getTimestamp()).append(',')
               .append(e.getLevel()).append(',')
               .append(csvEscape(e.getServiceName())).append(',')
               .append(csvEscape(e.getMessage())).append(',')
               .append(csvEscape(e.getExceptionType())).append(',')
               .append(csvEscape(e.getHost())).append(',')
               .append(csvEscape(e.getTraceId())).append('\n');
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportAsJson(ParsedLogEvent.LogLevel level, String serviceName, Instant from, Instant to) throws IOException {
        List<ParsedLogEvent> events = fetchEvents(level, from, to);

        if (serviceName != null && !serviceName.isBlank()) {
            String svcFilter = serviceName;
            events = events.stream()
                    .filter(e -> svcFilter.equalsIgnoreCase(e.getServiceName()))
                    .toList();
        }

        ObjectMapper writer = objectMapper.copy()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return writer.writeValueAsBytes(events);
    }

    private List<ParsedLogEvent> fetchEvents(ParsedLogEvent.LogLevel level, Instant from, Instant to) {
        Instant start = from != null ? from : Instant.EPOCH;
        Instant end   = to   != null ? to   : Instant.now();

        if (level != null) {
            return parsedLogEventRepository.findByLevelAndTimestampBetween(level, start, end)
                    .stream().limit(MAX_EXPORT_ROWS).toList();
        }
        return parsedLogEventRepository.findByTimestampBetween(start, end)
                .stream().limit(MAX_EXPORT_ROWS).toList();
    }

    private String csvEscape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
