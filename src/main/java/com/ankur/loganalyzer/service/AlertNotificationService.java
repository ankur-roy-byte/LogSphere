package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.model.AlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Sends alert notifications to configured channels (email and/or Slack).
 * Both channels are optional — disabled by default and guarded by config flags.
 * JavaMailSender is injected only when Spring Boot auto-configures it (mail host present).
 */
@Service
@Slf4j
public class AlertNotificationService {

    private final ApplicationProperties properties;
    private final RestTemplate restTemplate;

    @Nullable
    private JavaMailSender mailSender;

    @Nullable
    private AlertWebSocketService alertWebSocketService;

    public AlertNotificationService(ApplicationProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired(required = false)
    public void setAlertWebSocketService(AlertWebSocketService alertWebSocketService) {
        this.alertWebSocketService = alertWebSocketService;
    }

    @Async("ingestionExecutor")
    public void notifyAlert(AlertEvent event) {
        ApplicationProperties.Notification config = properties.getNotification();

        if (config.isEmailEnabled()) {
            sendEmail(event, config);
        }
        if (config.isSlackEnabled()) {
            sendSlackWebhook(event, config);
        }
        if (alertWebSocketService != null) {
            alertWebSocketService.broadcastAlert(event);
        }
    }

    private void sendEmail(AlertEvent event, ApplicationProperties.Notification config) {
        if (mailSender == null) {
            log.warn("Email notifications enabled but JavaMailSender not configured — skipping");
            return;
        }
        if (config.getEmailTo() == null || config.getEmailTo().isEmpty()) {
            log.warn("Email notifications enabled but no recipients configured — skipping");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(config.getEmailFrom());
            message.setTo(config.getEmailTo().toArray(new String[0]));
            message.setSubject(config.getEmailSubjectPrefix() + " " + event.getRule().getName());
            message.setText(buildEmailBody(event));
            mailSender.send(message);
            log.info("Alert email sent for rule '{}'", event.getRule().getName());
        } catch (Exception e) {
            log.error("Failed to send alert email for rule '{}'", event.getRule().getName(), e);
        }
    }

    private void sendSlackWebhook(AlertEvent event, ApplicationProperties.Notification config) {
        String webhookUrl = config.getSlackWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Slack notifications enabled but no webhook URL configured — skipping");
            return;
        }
        try {
            Map<String, String> payload = Map.of("text", buildSlackMessage(event));
            restTemplate.postForEntity(webhookUrl, payload, String.class);
            log.info("Slack notification sent for rule '{}'", event.getRule().getName());
        } catch (Exception e) {
            log.error("Failed to send Slack notification for rule '{}'", event.getRule().getName(), e);
        }
    }

    private String buildEmailBody(AlertEvent event) {
        return String.format("""
                LogSphere Alert Notification
                ============================
                Rule:         %s
                Condition:    %s
                Message:      %s
                Triggered At: %s

                Please investigate immediately.
                """,
                event.getRule().getName(),
                event.getRule().getConditionType(),
                event.getMessage(),
                event.getTriggeredAt());
    }

    private String buildSlackMessage(AlertEvent event) {
        return String.format(
                ":rotating_light: *[LogSphere Alert]* `%s`%n>*Condition:* %s%n>*Detail:* %s%n>*Time:* %s",
                event.getRule().getName(),
                event.getRule().getConditionType(),
                event.getMessage(),
                event.getTriggeredAt());
    }
}
