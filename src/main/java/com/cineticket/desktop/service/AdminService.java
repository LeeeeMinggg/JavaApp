package com.cineticket.desktop.service;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.dao.*;
import com.cineticket.desktop.model.User;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class AdminService {
    private final ConnectionFactory connectionFactory;
    private final UserDao userDao;

    public AdminService(ConnectionFactory connectionFactory, UserDao userDao) {
        this.connectionFactory = connectionFactory;
        this.userDao = userDao;
    }

    public DashboardStats dashboardStats() {
        String sql = """
                SELECT
                (SELECT COUNT(*) FROM users) total_users,
                (SELECT COUNT(*) FROM movies WHERE status='NOW_SHOWING') active_movies,
                (SELECT COUNT(*) FROM showtimes WHERE status='SCHEDULED' AND start_time>CURRENT_TIMESTAMP) future_showtimes,
                (SELECT COUNT(*) FROM bookings WHERE status='CONFIRMED') confirmed_bookings,
                (SELECT COUNT(*) FROM tickets WHERE status='ISSUED') issued_tickets,
                (SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='SUCCESS') total_revenue
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new DashboardStats(result.getLong("total_users"), result.getLong("active_movies"),
                    result.getLong("future_showtimes"), result.getLong("confirmed_bookings"),
                    result.getLong("issued_tickets"), result.getBigDecimal("total_revenue"));
        } catch (SQLException exception) { throw new DaoException("Không thể tải dashboard", exception); }
    }

    public List<RevenueRow> revenueByDay() {
        String sql = """
                SELECT CAST(payment_time AS DATE) revenue_date,COUNT(DISTINCT booking_id) bookings,
                       COUNT(*) payments,COALESCE(SUM(amount),0) revenue
                FROM payments WHERE status='SUCCESS' GROUP BY CAST(payment_time AS DATE) ORDER BY revenue_date DESC
                """;
        List<RevenueRow> rows = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) rows.add(new RevenueRow(result.getDate("revenue_date").toLocalDate(),
                    result.getLong("payments"), result.getLong("bookings"), result.getBigDecimal("revenue")));
            return rows;
        } catch (SQLException exception) { throw new DaoException("Không thể tải báo cáo doanh thu", exception); }
    }

    public List<User> users() { return userDao.findAll(); }
    public void setUserActive(String userId, boolean active) { userDao.setActive(userId, active); }
}
