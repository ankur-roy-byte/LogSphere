package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AlertEventResponse;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alert Management", description = "APIs for managing alert rules and retrieving alert events")
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/rules")
    @Operation(summary = "Create alert rule",
            description = "Create a new alert rule with threshold conditions")
    @ApiResponse(responseCode = "201", description = "Alert rule created successfully",
            content = @Content(schema = @Schema(implementation = AlertRule.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<AlertRule> createRule(@Valid @RequestBody AlertRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createRule(request));
    }

    @GetMapping("/rules")
    @Operation(summary = "Get all alert rules",
            description = "Retrieve all configured alert rules")
    @ApiResponse(responseCode = "200", description = "Alert rules retrieved successfully",
            content = @Content(schema = @Schema(implementation = AlertRule.class)))
    public ResponseEntity<List<AlertRule>> getAllRules() {
        return ResponseEntity.ok(alertService.getAllRules());
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete alert rule",
            description = "Delete an alert rule by ID")
    @ApiResponse(responseCode = "204", description = "Alert rule deleted successfully")
    @ApiResponse(responseCode = "404", description = "Alert rule not found")
    public ResponseEntity<Void> deleteRule(@Parameter(description = "Alert rule ID") @PathVariable Long id) {
        alertService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get alert events",
            description = "Retrieve paginated alert events with pagination support")
    @ApiResponse(responseCode = "200", description = "Alert events retrieved successfully",
            content = @Content(schema = @Schema(implementation = AlertEventResponse.class)))
    public ResponseEntity<Page<AlertEventResponse>> getAlertEvents(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(alertService.getAlertEvents(page, size));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve alert event",
            description = "Mark an active alert event as resolved so it leaves the unresolved queue")
    @ApiResponse(responseCode = "200", description = "Alert event resolved successfully",
            content = @Content(schema = @Schema(implementation = AlertEventResponse.class)))
    @ApiResponse(responseCode = "404", description = "Alert event not found")
    public ResponseEntity<AlertEventResponse> resolveAlertEvent(
            @Parameter(description = "Alert event ID") @PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolveAlertEvent(id));
    }

    @PostMapping("/test")
    @Operation(summary = "Test alert evaluation",
            description = "Manually trigger alert rule evaluation for testing purposes")
    @ApiResponse(responseCode = "200", description = "Alert evaluation completed successfully")
    public ResponseEntity<Map<String, String>> testAlertEvaluation() {
        alertService.evaluateRules();
        return ResponseEntity.ok(Map.of("status", "Alert evaluation completed"));
    }
}
