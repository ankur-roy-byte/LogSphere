package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.model.ApiKey;
import com.ankur.loganalyzer.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/api-keys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "API Keys", description = "Generate and manage API keys for programmatic access")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    @Operation(summary = "List all API keys")
    public ResponseEntity<ApiResponse<List<ApiKey>>> list() {
        return ResponseEntity.ok(ApiResponse.success(apiKeyService.listAll()));
    }

    @PostMapping
    @Operation(summary = "Generate a new API key", description = "Returns the raw key once — store it securely, it cannot be retrieved again")
    public ResponseEntity<ApiResponse<ApiKeyService.GeneratedKey>> generate(
            @RequestParam String name,
            Authentication auth) {
        String createdBy = auth != null ? auth.getName() : "system";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(apiKeyService.generate(name, createdBy)));
    }

    @PatchMapping("/{id}/revoke")
    @Operation(summary = "Revoke an API key", description = "Disables the key without deleting it — useful for audit trails")
    public ResponseEntity<Void> revoke(@PathVariable Long id) {
        apiKeyService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an API key permanently")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        apiKeyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
