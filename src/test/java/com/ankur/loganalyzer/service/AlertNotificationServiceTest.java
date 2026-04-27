package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.config.ApplicationProperties;
import com.ankur.loganalyzer.model.AlertEvent;
import com.ankur.loganalyzer.model.AlertRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServiceTest {

    @Mock private ApplicationProperties properties;
    @Mock private RestTemplate restTemplate;
    @Mock private JavaMailSender mailSender;

    private AlertNotificationService notificationService;

    private ApplicationProperties.Notification notificationConfig;
    private AlertEvent alertEvent;

    @BeforeEach
    void setUp() {
        notificationService = new AlertNotificationService(properties, restTemplate);

        notificationConfig = new ApplicationProperties.Notification();
        notificationConfig.setEmailEnabled(false);
        notificationConfig.setSlackEnabled(false);
        notificationConfig.setEmailFrom("alerts@logsphere.io");
        notificationConfig.setEmailSubjectPrefix("[LogSphere Alert]");
        notificationConfig.setEmailTo(List.of("oncall@example.com"));
        notificationConfig.setSlackWebhookUrl("https://hooks.slack.com/test");
        when(properties.getNotification()).thenReturn(notificationConfig);

        AlertRule rule = AlertRule.builder()
                .name("High Error Rate")
                .conditionType(AlertRule.ConditionType.ERROR_COUNT_EXCEEDS)
                .threshold(100)
                .build();
        alertEvent = AlertEvent.builder()
                .rule(rule)
                .message("Error count 150 exceeds threshold 100")
                .triggeredAt(Instant.now())
                .build();
    }

    @Test
    void notifyAlert_whenEmailEnabled_sendsEmailWithCorrectFields() {
        notificationConfig.setEmailEnabled(true);
        notificationService.setMailSender(mailSender);

        notificationService.notifyAlert(alertEvent);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertThat(sent.getFrom()).isEqualTo("alerts@logsphere.io");
        assertThat(sent.getTo()).containsExactly("oncall@example.com");
        assertThat(sent.getSubject()).contains("High Error Rate");
        assertThat(sent.getText()).contains("High Error Rate", "ERROR_COUNT_EXCEEDS");
    }

    @Test
    void notifyAlert_whenEmailEnabledButNoMailSender_doesNotThrow() {
        notificationConfig.setEmailEnabled(true);
        // mailSender NOT injected — simulates unconfigured mail server

        notificationService.notifyAlert(alertEvent);

        verifyNoInteractions(mailSender);
    }

    @Test
    void notifyAlert_whenEmailDisabled_neverCallsMailSender() {
        notificationConfig.setEmailEnabled(false);
        notificationService.setMailSender(mailSender);

        notificationService.notifyAlert(alertEvent);

        verifyNoInteractions(mailSender);
    }

    @Test
    void notifyAlert_whenSlackEnabled_postsJsonPayloadToWebhookUrl() {
        notificationConfig.setSlackEnabled(true);

        notificationService.notifyAlert(alertEvent);

        verify(restTemplate).postForEntity(
                eq("https://hooks.slack.com/test"),
                any(),
                eq(String.class)
        );
    }

    @Test
    void notifyAlert_whenSlackEnabledButEmptyUrl_skipsWebhookCall() {
        notificationConfig.setSlackEnabled(true);
        notificationConfig.setSlackWebhookUrl("");

        notificationService.notifyAlert(alertEvent);

        verifyNoInteractions(restTemplate);
    }
}
