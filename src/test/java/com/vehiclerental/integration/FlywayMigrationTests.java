package com.vehiclerental.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest

@ActiveProfiles("test")
public class FlywayMigrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testFlywayMigrationCreatedTables() {
        // Query H2 information schema to ensure tables were created by Flyway
        int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('USERS', 'VEHICLES', 'BOOKINGS', 'PAYMENTS', 'MAINTENANCE_RECORDS')",
                Integer.class
        );
        // We expect 5 tables
        assertTrue(count >= 5, "Flyway should have created the expected tables");
    }
}
