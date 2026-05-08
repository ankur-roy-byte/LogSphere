package com.ankur.loganalyzer.controller;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.dto.ApiResponse;
import com.ankur.loganalyzer.dto.NotificationTestRequest;
import com.ankur.loganalyzer.model.AlertEvent;
import com.ankur.loganalyzer.model.AlertRule;
import com.ankur.loganalyzer.service.AlertNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification channel management and testing")
public class NotificationController {

    private final AlertNotificationService alertNotificationService;
    private final ApplicationProperties properties;

    @PostMapping("/test")
    @Operation(
        summary = "Send a test notification",
        description = "Fires a dummy alert through the specified channel to verify your config is working"
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> test(
            @Valid @RequestBody NotificationTestRequest request) {

        ApplicationProperties.Notification cfg = properties.getNotification();

        AlertRule rule = AlertRule.builder()
                .name("Test Alert")
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS)
                .threshold(0)
                .build();

        AlertEvent event = AlertEvent.builder()
                .rule(rule)
                .message("This is a test notification from LogSphere. If you received this, your channel config is working correctly.")
                .triggeredAt(Instant.now())
                .build();

        boolean emailSent = false;
        boolean slackSent = false;

        NotificationTestRequest.Channel ch = request.channel();

        if (ch == NotificationTestRequest.Channel.EMAIL || ch == NotificationTestRequest.Channel.ALL) {
            if (cfg.isEmailEnabled()) {
                alertNotificationService.notifyAlert(event);
                emailSent = true;
            }
        }
        if (ch == NotificationTestRequest.Channel.SLACK || ch == NotificationTestRequest.Channel.ALL) {
            if (cfg.isSlackEnabled()) {
                alertNotificationService.notifyAlert(event);
                slackSent = true;
            }
        }

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "channel", request.channel().name(),
                "emailSent", emailSent,
                "slackSent", slackSent,
                "note", (!emailSent && !slackSent)
                        ? "No channels are enabled — check app.notification.* in application.yml"
                        : "Test notification dispatched"
        )));
    }
}
