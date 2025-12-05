package com.mediapp.doctor_service.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import jakarta.annotation.PostConstruct;

/**
 * Ensures the database exists before JPA tries to connect.
 * JPA will handle schema creation via hibernate.ddl-auto.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializerConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/mediapp_doctor?createDatabaseIfNotExist=true}")
    private String jdbcUrl;

    @Value("${spring.datasource.username:root}")
    private String username;

    @Value("${spring.datasource.password:}")
    private String password;

    @PostConstruct
    public void initializeDatabase() {
        String databaseName = extractDatabaseName(jdbcUrl);
        String baseUrl = extractBaseUrl(jdbcUrl);

        log.info("Ensuring database '{}' exists...", databaseName);

        try (Connection connection = DriverManager.getConnection(baseUrl, username, password);
                Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
            log.info("Database '{}' is ready", databaseName);

        } catch (SQLException e) {
            log.error("Failed to create database '{}': {}", databaseName, e.getMessage());
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    private String extractDatabaseName(String url) {
        String withoutParams = url.split("\\?")[0];
        String[] parts = withoutParams.split("/");
        return parts[parts.length - 1];
    }

    private String extractBaseUrl(String url) {
        String withoutParams = url.split("\\?")[0];
        int lastSlash = withoutParams.lastIndexOf('/');
        String baseUrl = withoutParams.substring(0, lastSlash);
        return baseUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}
