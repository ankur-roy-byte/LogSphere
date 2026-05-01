package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.dto.CreateUserRequest;
import com.ankur.loganalyzer.dto.UserResponse;
import com.ankur.loganalyzer.exception.ResourceNotFoundException;
import com.ankur.loganalyzer.model.AppUser;
import com.ankur.loganalyzer.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> listAll() {
        return appUserRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findOrThrow(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }
        AppUser user = AppUser.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();
        return UserResponse.from(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        AppUser user = findOrThrow(id);
        user.setEnabled(enabled);
        return UserResponse.from(appUserRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        appUserRepository.deleteById(id);
    }

    private AppUser findOrThrow(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
