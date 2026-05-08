package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.exception.ResourceNotFoundException;
import com.ankur.loganalyzer.model.ApiKey;
import com.ankur.loganalyzer.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public record GeneratedKey(Long id, String name, String rawKey, Instant createdAt) {}

    @Transactional
    public GeneratedKey generate(String name, String createdBy) {
        String rawKey = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String hash = sha256(rawKey);

        ApiKey key = ApiKey.builder()
                .name(name)
                .keyHash(hash)
                .createdBy(createdBy)
                .build();
        ApiKey saved = apiKeyRepository.save(key);
        return new GeneratedKey(saved.getId(), saved.getName(), rawKey, saved.getCreatedAt());
    }

    public Optional<ApiKey> validate(String rawKey) {
        String hash = sha256(rawKey);
        return apiKeyRepository.findByKeyHash(hash)
                .filter(ApiKey::isEnabled)
                .filter(k -> k.getExpiresAt() == null || k.getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    public void recordUsage(ApiKey key) {
        key.setLastUsedAt(Instant.now());
        apiKeyRepository.save(key);
    }

    public List<ApiKey> listAll() {
        return apiKeyRepository.findAll();
    }

    @Transactional
    public void revoke(Long id) {
        ApiKey key = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found: " + id));
        key.setEnabled(false);
        apiKeyRepository.save(key);
    }

    @Transactional
    public void delete(Long id) {
        if (!apiKeyRepository.existsById(id)) {
            throw new ResourceNotFoundException("API key not found: " + id);
        }
        apiKeyRepository.deleteById(id);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
