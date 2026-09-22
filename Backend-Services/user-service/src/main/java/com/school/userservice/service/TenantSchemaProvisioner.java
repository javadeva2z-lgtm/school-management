package com.school.userservice.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import com.school.userservice.dto.SchoolPartialDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TenantSchemaProvisioner {

    private static final String SCHOOL_INSERT_SQL = "INSERT INTO `schools` (`school_code`,`school_name`, `keywords`, `is_active`,`created_at`, updated_by) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    public void provisionSchema(SchoolPartialDTO school) {

        String schemaName = school.getSchoolCode();
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

            try (PreparedStatement ps = connection.prepareStatement(SCHOOL_INSERT_SQL)) {
                ps.setString(1, school.getSchoolCode());
                ps.setString(2, school.getSchoolName());
                ps.setString(3, school.getKeywords());
                ps.setBoolean(4, true);
                ps.setDate(5, Date.valueOf(LocalDate.now()));
                ps.setString(6, "superadmin");

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to provision schema for: " + schemaName, e);
        }
    }
}