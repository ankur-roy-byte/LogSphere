package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.dto.AlertEventResponse;
import com.ankur.loganalyzer.dto.AlertRuleRequest;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.service.AlertService;
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
public class AlertController {

    private final AlertService alertService;

    @PostMapping("/rules")
    public ResponseEntity<AlertRule> createRule(@Valid @RequestBody AlertRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertService.createRule(request));
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AlertRule>> getAllRules() {
        return ResponseEntity.ok(alertService.getAllRules());
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        alertService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<AlertEventResponse>> getAlertEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(alertService.getAlertEvents(page, size));
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testAlertEvaluation() {
        alertService.evaluateRules();
        return ResponseEntity.ok(Map.of("status", "Alert evaluation completed"));
    }
}
