package com.mediapp.doctor_service.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;

/**
 * Ensures the database and schema exist before R2DBC tries to connect.
 * R2DBC does not support createDatabaseIfNotExist, so we use JDBC
 * to create the database and tables at startup if they don't exist.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseInitializerConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializerConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/mediapp_doctor?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
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

        // First, create the database if it doesn't exist
        try (Connection connection = DriverManager.getConnection(baseUrl, username, password);
                Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);
            log.info("Database '{}' is ready", databaseName);

        } catch (SQLException e) {
            log.error("Failed to create database '{}': {}", databaseName, e.getMessage());
            throw new RuntimeException("Could not initialize database", e);
        }

        // Then, run the schema initialization script
        runSchemaScript(databaseName);
    }

    private void runSchemaScript(String databaseName) {
        String schemaUrl = jdbcUrl.contains("?") ? jdbcUrl
                : jdbcUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection connection = DriverManager.getConnection(schemaUrl, username, password);
                Statement statement = connection.createStatement()) {

            String schemaSql = loadSchemaScript();
            if (schemaSql != null && !schemaSql.isBlank()) {
                // Split by semicolon and execute each statement
                String[] statements = schemaSql.split(";");
                for (String sql : statements) {
                    String trimmedSql = sql.trim();
                    if (!trimmedSql.isEmpty()) {
                        try {
                            statement.execute(trimmedSql);
                        } catch (SQLException e) {
                            // Log but don't fail for "already exists" type errors
                            if (!e.getMessage().contains("already exists") && !e.getMessage().contains("Duplicate")) {
                                log.warn("SQL statement warning: {}", e.getMessage());
                            }
                        }
                    }
                }
                log.info("Schema initialization completed for database '{}'", databaseName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize schema: {}", e.getMessage());
            throw new RuntimeException("Could not initialize schema", e);
        }
    }

    private String loadSchemaScript() {
        try {
            ClassPathResource resource = new ClassPathResource("db/schema.sql");
            try (InputStream is = resource.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            log.warn("No schema.sql found in classpath, skipping schema initialization");
            return null;
        }
    }

    private String extractDatabaseName(String url) {
        // Extract database name from JDBC URL like
        // jdbc:mysql://localhost:3306/mediapp_doctor?...
        String withoutParams = url.split("\\?")[0];
        String[] parts = withoutParams.split("/");
        return parts[parts.length - 1];
    }

    private String extractBaseUrl(String url) {
        // Get the base URL without the database name, connecting to MySQL server
        // directly
        String withoutParams = url.split("\\?")[0];
        int lastSlash = withoutParams.lastIndexOf('/');
        String baseUrl = withoutParams.substring(0, lastSlash);
        // Add parameters for connection
        return baseUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}
