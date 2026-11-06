package com.cineticket.desktop.service;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.config.DriverManagerConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

final class TestDatabase {
    private TestDatabase() { }

    static ConnectionFactory create() {
        String name = "cineticket_" + UUID.randomUUID().toString().replace("-", "");
        ConnectionFactory factory = new DriverManagerConnectionFactory(
                "jdbc:h2:mem:" + name + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000", "sa", "");
        try (Connection connection = factory.getConnection(); Statement statement = connection.createStatement()) {
            String script = new String(TestDatabase.class.getResourceAsStream("/schema-test.sql").readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : script.split(";")) if (!sql.isBlank()) statement.execute(sql.trim());
            return factory;
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot initialize test database", exception);
        }
    }
}
