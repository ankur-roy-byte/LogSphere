package com.ankur.loganalyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for outbound notification infrastructure.
 * RestTemplate is used by AlertNotificationService for Slack webhook calls.
 */
@Configuration
public class NotificationConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
