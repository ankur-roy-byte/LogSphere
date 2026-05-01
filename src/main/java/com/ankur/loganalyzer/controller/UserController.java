package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.CreateUserRequest;
import com.ankur.loganalyzer.dto.UserResponse;
import com.ankur.loganalyzer.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "User Management", description = "Admin-only user CRUD")
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @Operation(summary = "List all users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.listAll()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userManagementService.create(request)));
    }

    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<ApiResponse<UserResponse>> setEnabled(
            @PathVariable Long id,
            @RequestParam boolean value) {
        return ResponseEntity.ok(ApiResponse.success(userManagementService.setEnabled(id, value)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userManagementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
