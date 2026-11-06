package com.cineticket.desktop.config;

import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DatabaseInitializer {
    private final ConnectionFactory connectionFactory;

    public DatabaseInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void initialize() {
        try (Connection connection = connectionFactory.getConnection()) {
            runScript(connection, "/database/h2-schema.sql");
            if (isEmpty(connection)) runScript(connection, "/database/h2-seed.sql");
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể khởi tạo database H2", exception);
        }
    }

    private boolean isEmpty(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM users")) {
            result.next();
            return result.getInt(1) == 0;
        }
    }

    private void runScript(Connection connection, String resourcePath) throws Exception {
        InputStream input = DatabaseInitializer.class.getResourceAsStream(resourcePath);
        if (input == null) throw new IllegalStateException("Thiếu file database: " + resourcePath);
        try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            RunScript.execute(connection, reader);
        }
    }
}
