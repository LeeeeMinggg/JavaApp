package com.cineticket.desktop.service;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.dao.DaoException;
import com.cineticket.desktop.dao.MovieDao;
import com.cineticket.desktop.dao.ShowtimeDao;
import com.cineticket.desktop.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class ManagementService {
    private final ConnectionFactory connectionFactory;
    private final MovieDao movieDao;
    private final ShowtimeDao showtimeDao;

    public ManagementService(ConnectionFactory connectionFactory, MovieDao movieDao, ShowtimeDao showtimeDao) {
        this.connectionFactory = connectionFactory;
        this.movieDao = movieDao;
        this.showtimeDao = showtimeDao;
    }

    public void saveMovie(Movie movie, boolean existing) {
        if (existing) movieDao.update(movie); else movieDao.save(movie);
    }

    public List<Room> rooms() {
        String sql = """
                SELECT r.*,c.name cinema_name,c.address FROM rooms r JOIN cinemas c ON c.cinema_id=r.cinema_id
                WHERE r.status='ACTIVE' ORDER BY c.name,r.room_name
                """;
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Cinema cinema = new Cinema(result.getString("cinema_id"), result.getString("cinema_name"), result.getString("address"));
                rooms.add(new Room(result.getString("room_id"), cinema, result.getString("room_name"), result.getString("status")));
            }
            return rooms;
        } catch (SQLException exception) { throw new DaoException("Không thể tải danh sách phòng", exception); }
    }

    public void createShowtime(Showtime showtime) {
        validateNoOverlap(showtime.getRoom().getRoomId(), showtime.getStartTime(), showtime.getEndTime());
        showtimeDao.save(showtime);
    }

    private void validateNoOverlap(String roomId, LocalDateTime start, LocalDateTime end) {
        String sql = """
                SELECT COUNT(*) FROM showtimes WHERE room_id=? AND status='SCHEDULED'
                AND start_time<? AND end_time>?
                """;
        try (Connection connection = connectionFactory.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomId); statement.setTimestamp(2, Timestamp.valueOf(end)); statement.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) > 0) throw new IllegalStateException("Phòng đã có suất chiếu trùng thời gian");
            }
        } catch (SQLException exception) { throw new DaoException("Không thể kiểm tra lịch phòng", exception); }
    }
}
