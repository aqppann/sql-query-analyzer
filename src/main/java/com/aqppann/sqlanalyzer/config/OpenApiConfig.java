package com.aqppann.sqlanalyzer.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SQL Query Performance Analyzer API")
                        .version("1.0.0")
                        .description("REST API for analyzing SQL query performance. " +
                                "Classifies queries as NORMAL, SLOW or CRITICAL " +
                                "and provides optimization recommendations.")
                        .contact(new Contact()
                                .name("SQL Analyzer")
                                .email("sql-analyzer@example.com")));
    }
}