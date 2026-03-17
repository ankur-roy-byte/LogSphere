package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.ParsedLogResponse;
import com.ankur.loganalyzer.model.ParsedLogEvent;
import com.ankur.loganalyzer.repository.ParsedLogEventRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogSearchService {

    private final ParsedLogEventRepository parsedLogEventRepository;

    public Page<ParsedLogResponse> searchLogs(String serviceName, String level, String traceId,
                                               String keyword, String host,
                                               Instant startTime, Instant endTime,
                                               int page, int size) {
        Specification<ParsedLogEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (serviceName != null && !serviceName.isBlank()) {
                predicates.add(cb.equal(root.get("serviceName"), serviceName));
            }
            if (level != null && !level.isBlank()) {
                predicates.add(cb.equal(root.get("level"), ParsedLogEvent.LogLevel.valueOf(level.toUpperCase())));
            }
            if (traceId != null && !traceId.isBlank()) {
                predicates.add(cb.equal(root.get("traceId"), traceId));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("message")), "%" + keyword.toLowerCase() + "%"));
            }
            if (host != null && !host.isBlank()) {
                predicates.add(cb.equal(root.get("host"), host));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return parsedLogEventRepository.findAll(spec, pageRequest).map(this::toResponse);
    }

    public ParsedLogResponse getById(Long id) {
        ParsedLogEvent event = parsedLogEventRepository.findById(id)
                .orElseThrow(() -> new com.ankur.loganalyzer.exception.ResourceNotFoundException("ParsedLogEvent", id));
        return toResponse(event);
    }

    private ParsedLogResponse toResponse(ParsedLogEvent event) {
        return ParsedLogResponse.builder()
                .id(event.getId())
                .serviceName(event.getServiceName())
                .level(event.getLevel().name())
                .message(event.getMessage())
                .exceptionType(event.getExceptionType())
                .stackTrace(event.getStackTrace())
                .timestamp(event.getTimestamp())
                .traceId(event.getTraceId())
                .host(event.getHost())
                .metadata(event.getMetadata())
                .build();
    }
}
