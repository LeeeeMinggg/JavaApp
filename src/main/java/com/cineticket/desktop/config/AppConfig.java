package com.cineticket.desktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public final class AppConfig {
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;
    private final Duration seatHoldDuration;
    private final boolean paymentSimulationEnabled;

    private AppConfig(String databaseUrl, String databaseUser, String databasePassword,
                      Duration seatHoldDuration, boolean paymentSimulationEnabled) {
        this.databaseUrl = databaseUrl;
        this.databaseUser = databaseUser;
        this.databasePassword = databasePassword;
        this.seatHoldDuration = seatHoldDuration;
        this.paymentSimulationEnabled = paymentSimulationEnabled;
    }

    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream input = AppConfig.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy application.properties");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể đọc cấu hình ứng dụng", exception);
        }

        String url = environmentOrDefault("CINETICKET_DB_URL", properties.getProperty("db.url"));
        String user = environmentOrDefault("CINETICKET_DB_USER", properties.getProperty("db.user"));
        String password = environmentOrDefault("CINETICKET_DB_PASSWORD", properties.getProperty("db.password", ""));
        long holdMinutes = Long.parseLong(properties.getProperty("booking.hold.minutes", "5"));
        boolean simulated = Boolean.parseBoolean(properties.getProperty("payment.simulation.enabled", "true"));
        return new AppConfig(url, user, password, Duration.ofMinutes(holdMinutes), simulated);
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public String databaseUrl() { return databaseUrl; }
    public String databaseUser() { return databaseUser; }
    public String databasePassword() { return databasePassword; }
    public Duration seatHoldDuration() { return seatHoldDuration; }
    public boolean paymentSimulationEnabled() { return paymentSimulationEnabled; }
}
