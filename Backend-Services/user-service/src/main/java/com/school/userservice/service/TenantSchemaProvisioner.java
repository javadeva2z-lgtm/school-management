package com.school.userservice.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TenantSchemaProvisioner {

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    public void provisionSchema(String schemaName) {
        // 1. Basic sanitization to prevent SQL injection on the schema name
        if (!schemaName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid schema name");
        }

        try (Connection connection = dataSource.getConnection()) {
            // 2. Create the new schema
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            }

            // 3. Set the active schema for this connection session
            // Note: Use "USE " + schemaName for MySQL/MariaDB
            try (Statement statement = connection.createStatement()) {
                statement.execute("USE " + schemaName);
            }

            // 4. Load and execute the tenant schema SQL from the classpath
            Resource resource = resourceLoader.getResource("classpath:/database/schema.sql");
            ScriptUtils.executeSqlScript(connection, resource);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to provision schema for: " + schemaName, e);
        }
    }
}