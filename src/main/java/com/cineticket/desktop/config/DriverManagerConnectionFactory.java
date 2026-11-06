package com.cineticket.desktop.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DriverManagerConnectionFactory implements ConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public DriverManagerConnectionFactory(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
