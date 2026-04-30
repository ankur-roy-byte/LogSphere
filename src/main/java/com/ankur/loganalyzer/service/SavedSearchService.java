package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.LogSearchRequest;
import com.ankur.loganalyzer.dto.SavedSearchRequest;
import com.ankur.loganalyzer.dto.SavedSearchResponse;
import com.ankur.loganalyzer.exception.ResourceNotFoundException;
import com.ankur.loganalyzer.model.SavedSearch;
import com.ankur.loganalyzer.repository.SavedSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedSearchService {

    private static final String SYSTEM_USER = "system";

    private final SavedSearchRepository savedSearchRepository;

    @Transactional
    public SavedSearchResponse create(SavedSearchRequest request) {
        SavedSearch savedSearch = SavedSearch.builder()
                .name(request.name().trim())
                .description(blankToNull(request.description()))
                .serviceName(blankToNull(request.serviceName()))
                .level(request.level() == null ? null : request.level().toUpperCase())
                .traceId(blankToNull(request.traceId()))
                .keyword(blankToNull(request.keyword()))
                .host(blankToNull(request.host()))
                .startTime(request.startTime())
                .endTime(request.endTime())
                .createdBy(currentUsername())
                .build();

        return toResponse(savedSearchRepository.save(savedSearch));
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> list() {
        return savedSearchRepository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SavedSearchResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public LogSearchRequest toLogSearchRequest(Long id, int page, int size) {
        SavedSearch savedSearch = getEntity(id);
        return LogSearchRequest.builder()
                .serviceName(savedSearch.getServiceName())
                .level(savedSearch.getLevel())
                .traceId(savedSearch.getTraceId())
                .keyword(savedSearch.getKeyword())
                .host(savedSearch.getHost())
                .startTime(savedSearch.getStartTime())
                .endTime(savedSearch.getEndTime())
                .page(page)
                .size(size)
                .build();
    }

    @Transactional
    public void delete(Long id) {
        if (!savedSearchRepository.existsById(id)) {
            throw new ResourceNotFoundException("SavedSearch", id);
        }
        savedSearchRepository.deleteById(id);
    }

    private SavedSearch getEntity(Long id) {
        return savedSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", id));
    }

    private SavedSearchResponse toResponse(SavedSearch savedSearch) {
        return SavedSearchResponse.builder()
                .id(savedSearch.getId())
                .name(savedSearch.getName())
                .description(savedSearch.getDescription())
                .serviceName(savedSearch.getServiceName())
                .level(savedSearch.getLevel())
                .traceId(savedSearch.getTraceId())
                .keyword(savedSearch.getKeyword())
                .host(savedSearch.getHost())
                .startTime(savedSearch.getStartTime())
                .endTime(savedSearch.getEndTime())
                .createdBy(savedSearch.getCreatedBy())
                .createdAt(savedSearch.getCreatedAt())
                .updatedAt(savedSearch.getUpdatedAt())
                .build();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            return SYSTEM_USER;
        }
        return authentication.getName();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
