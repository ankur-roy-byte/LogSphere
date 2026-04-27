package com.ankur.loganalyzer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogSphere - Log Analysis Platform API")
                        .description("Enterprise log ingestion, parsing, and analysis platform. " +
                                "Ingest logs from multiple sources, parse them into structured format, " +
                                "and gain insights through advanced analytics and anomaly detection.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support Team")
                                .url("https://github.com/ankur-roy-byte/LogSphere"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.logsphere.com")
                                .description("Production")
                ));
    }
}
