package com.cineticket.desktop.dao;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcShowtimeDao implements ShowtimeDao {
    private static final String BASE_SELECT = """
            SELECT st.*, m.title,m.description,m.genre,m.duration,m.release_date,m.status AS movie_status,m.poster_url,
                   r.room_name,r.status AS room_status,c.cinema_id,c.name AS cinema_name,c.address
            FROM showtimes st JOIN movies m ON m.movie_id=st.movie_id
            JOIN rooms r ON r.room_id=st.room_id JOIN cinemas c ON c.cinema_id=r.cinema_id
            """;
    private final ConnectionFactory connectionFactory;
    public JdbcShowtimeDao(ConnectionFactory connectionFactory) { this.connectionFactory = connectionFactory; }

    @Override public List<Showtime> findByMovie(String movieId) {
        return query(BASE_SELECT + " WHERE st.movie_id=? AND st.status='SCHEDULED' AND st.start_time>NOW() ORDER BY st.start_time", movieId);
    }
    @Override public List<Showtime> findFuture() {
        return query(BASE_SELECT + " WHERE st.start_time>NOW() ORDER BY st.start_time", null);
    }
    @Override public Optional<Showtime> findById(String showtimeId) {
        List<Showtime> values = query(BASE_SELECT + " WHERE st.showtime_id=?", showtimeId);
        return values.stream().findFirst();
    }

    @Override
    public List<SeatView> findSeats(String showtimeId) {
        String sql = """
                SELECT ss.showtime_seat_id,ss.seat_id,s.seat_number,s.type,ss.status,ss.held_until,ss.price,ss.version
                FROM showtime_seats ss JOIN seats s ON s.seat_id=ss.seat_id
                WHERE ss.showtime_id=? ORDER BY s.seat_number
                """;
        List<SeatView> seats = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, showtimeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Timestamp held = result.getTimestamp("held_until");
                    seats.add(new SeatView(result.getString("showtime_seat_id"), result.getString("seat_id"),
                            result.getString("seat_number"), SeatType.valueOf(result.getString("type")),
                            SeatStatus.valueOf(result.getString("status")), held == null ? null : held.toLocalDateTime(),
                            result.getBigDecimal("price"), result.getInt("version")));
                }
            }
            return seats;
        } catch (SQLException exception) { throw new DaoException("Không thể tải sơ đồ ghế", exception); }
    }

    @Override
    public void save(Showtime showtime) {
        String insert = "INSERT INTO showtimes(showtime_id,movie_id,room_id,start_time,end_time,status,base_price) VALUES(?,?,?,?,?,?,?)";
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(insert)) {
                statement.setString(1, showtime.getShowtimeId()); statement.setString(2, showtime.getMovie().getMovieId());
                statement.setString(3, showtime.getRoom().getRoomId()); statement.setTimestamp(4, Timestamp.valueOf(showtime.getStartTime()));
                statement.setTimestamp(5, Timestamp.valueOf(showtime.getEndTime())); statement.setString(6, showtime.getStatus().name());
                statement.setBigDecimal(7, showtime.getBasePrice()); statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO showtime_seats(showtime_seat_id,showtime_id,seat_id,status,price,version)
                    SELECT CONCAT(?, '-', seat_id), ?, seat_id, 'AVAILABLE',
                           CASE type WHEN 'VIP' THEN ?*1.4 WHEN 'COUPLE' THEN ?*2 ELSE ? END, 0
                    FROM seats WHERE room_id=?
                    """)) {
                statement.setString(1, showtime.getShowtimeId()); statement.setString(2, showtime.getShowtimeId());
                statement.setBigDecimal(3, showtime.getBasePrice()); statement.setBigDecimal(4, showtime.getBasePrice());
                statement.setBigDecimal(5, showtime.getBasePrice()); statement.setString(6, showtime.getRoom().getRoomId());
                statement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException exception) { throw new DaoException("Không thể tạo suất chiếu", exception); }
    }

    private List<Showtime> query(String sql, String parameter) {
        List<Showtime> showtimes = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) statement.setString(1, parameter);
            try (ResultSet result = statement.executeQuery()) { while (result.next()) showtimes.add(map(result)); }
            return showtimes;
        } catch (SQLException exception) { throw new DaoException("Không thể tải suất chiếu", exception); }
    }

    static Showtime map(ResultSet result) throws SQLException {
        Movie movie = new Movie(result.getString("movie_id"), result.getString("title"), result.getString("description"),
                result.getString("genre"), result.getInt("duration"), result.getDate("release_date").toLocalDate(),
                MovieStatus.valueOf(result.getString("movie_status")), result.getString("poster_url"));
        Cinema cinema = new Cinema(result.getString("cinema_id"), result.getString("cinema_name"), result.getString("address"));
        Room room = new Room(result.getString("room_id"), cinema, result.getString("room_name"), result.getString("room_status"));
        return new Showtime(result.getString("showtime_id"), movie, room,
                result.getTimestamp("start_time").toLocalDateTime(), result.getTimestamp("end_time").toLocalDateTime(),
                ShowtimeStatus.valueOf(result.getString("status")), result.getBigDecimal("base_price"));
    }
}
