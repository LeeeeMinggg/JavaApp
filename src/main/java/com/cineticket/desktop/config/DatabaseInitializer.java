package com.cineticket.desktop.config;

import org.h2.tools.RunScript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DatabaseInitializer {
    private final ConnectionFactory connectionFactory;

    public DatabaseInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void initialize() {
        try (Connection connection = connectionFactory.getConnection()) {
            runScript(connection, "/database/h2-schema.sql");
            if (isEmpty(connection)) runScript(connection, "/database/h2-seed.sql");
            ensureUpcomingShowtimes(connection);
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

    private void ensureUpcomingShowtimes(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT COUNT(*) FROM showtimes WHERE status='SCHEDULED' AND start_time>CURRENT_TIMESTAMP")) {
            result.next();
            if (result.getInt(1) > 0) return;
        }

        String[][] schedule = {
                {"movie-01", "room-01", "0", "166", "90000"},
                {"movie-01", "room-02", "8", "166", "100000"},
                {"movie-02", "room-03", "1", "131", "85000"},
                {"movie-03", "room-01", "24", "100", "75000"},
                {"movie-04", "room-02", "25", "94", "80000"},
                {"movie-05", "room-03", "32", "115", "95000"}
        };
        String prefix = "demo-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        LocalDateTime firstStart = LocalDate.now().plusDays(1).atTime(10, 0);

        connection.setAutoCommit(false);
        try {
            String showtimeSql = """
                    INSERT INTO showtimes(showtime_id,movie_id,room_id,start_time,end_time,status,base_price)
                    VALUES(?,?,?,?,?,'SCHEDULED',?)
                    """;
            String seatsSql = """
                    INSERT INTO showtime_seats(showtime_seat_id,showtime_id,seat_id,status,price,version)
                    SELECT CONCAT(?, '-', seat_id), ?, seat_id, 'AVAILABLE',
                           CASE type WHEN 'VIP' THEN ?*1.4 WHEN 'COUPLE' THEN ?*2 ELSE ? END, 0
                    FROM seats WHERE room_id=?
                    """;
            for (int index = 0; index < schedule.length; index++) {
                String[] item = schedule[index];
                String showtimeId = prefix + "-" + String.format("%02d", index + 1);
                LocalDateTime start = firstStart.plusHours(Long.parseLong(item[2]));
                LocalDateTime end = start.plusMinutes(Long.parseLong(item[3]));
                java.math.BigDecimal price = new java.math.BigDecimal(item[4]);
                try (PreparedStatement statement = connection.prepareStatement(showtimeSql)) {
                    statement.setString(1, showtimeId);
                    statement.setString(2, item[0]);
                    statement.setString(3, item[1]);
                    statement.setTimestamp(4, Timestamp.valueOf(start));
                    statement.setTimestamp(5, Timestamp.valueOf(end));
                    statement.setBigDecimal(6, price);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(seatsSql)) {
                    statement.setString(1, showtimeId);
                    statement.setString(2, showtimeId);
                    statement.setBigDecimal(3, price);
                    statement.setBigDecimal(4, price);
                    statement.setBigDecimal(5, price);
                    statement.setString(6, item[1]);
                    statement.executeUpdate();
                }
            }
            connection.commit();
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
