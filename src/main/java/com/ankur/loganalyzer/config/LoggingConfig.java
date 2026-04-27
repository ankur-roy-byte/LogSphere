package com.ankur.loganalyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@Configuration
public class LoggingConfig {

    @Bean
    @Profile("!prod")
    public LoggingAudit loggingAudit() {
        return new LoggingAudit();
    }

    public static class LoggingAudit {
        // This bean allows for enhanced logging in non-production environments
    }
}
