package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.LogSourceRequest;
import com.ankur.loganalyzer.dto.LogSourceResponse;
import com.ankur.loganalyzer.exception.ResourceNotFoundException;
import com.ankur.loganalyzer.model.LogSource;
import com.ankur.loganalyzer.repository.LogSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogSourceService {

    private final LogSourceRepository logSourceRepository;

    public List<LogSourceResponse> listAll() {
        return logSourceRepository.findAll().stream().map(LogSourceResponse::from).toList();
    }

    public List<LogSourceResponse> listActive() {
        return logSourceRepository.findByActiveTrue().stream().map(LogSourceResponse::from).toList();
    }

    public LogSourceResponse getById(Long id) {
        return LogSourceResponse.from(findOrThrow(id));
    }

    @Transactional
    public LogSourceResponse create(LogSourceRequest request) {
        LogSource source = LogSource.builder()
                .name(request.name())
                .type(request.type())
                .configJson(request.configJson())
                .build();
        return LogSourceResponse.from(logSourceRepository.save(source));
    }

    @Transactional
    public LogSourceResponse update(Long id, LogSourceRequest request) {
        LogSource source = findOrThrow(id);
        source.setName(request.name());
        source.setType(request.type());
        source.setConfigJson(request.configJson());
        return LogSourceResponse.from(logSourceRepository.save(source));
    }

    @Transactional
    public LogSourceResponse toggleActive(Long id) {
        LogSource source = findOrThrow(id);
        source.setActive(!source.isActive());
        return LogSourceResponse.from(logSourceRepository.save(source));
    }

    @Transactional
    public void delete(Long id) {
        if (!logSourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Log source not found: " + id);
        }
        logSourceRepository.deleteById(id);
    }

    private LogSource findOrThrow(Long id) {
        return logSourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Log source not found: " + id));
    }
}
