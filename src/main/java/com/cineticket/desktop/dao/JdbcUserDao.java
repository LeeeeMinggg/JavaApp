package com.cineticket.desktop.dao;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcUserDao implements UserDao {
    private final ConnectionFactory connectionFactory;

    public JdbcUserDao(ConnectionFactory connectionFactory) { this.connectionFactory = connectionFactory; }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("Không thể tìm tài khoản", exception);
        }
    }

    @Override
    public Optional<User> findById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DaoException("Không thể tìm tài khoản", exception);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users ORDER BY role, full_name");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) users.add(map(result));
            return users;
        } catch (SQLException exception) {
            throw new DaoException("Không thể tải danh sách tài khoản", exception);
        }
    }

    @Override
    public void createCustomer(User customer) {
        String sql = "INSERT INTO users(user_id, username, password_hash, full_name, email, role, active) VALUES(?,?,?,?,?,'CUSTOMER',TRUE)";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customer.getUserId());
            statement.setString(2, customer.getUsername());
            statement.setString(3, customer.getPasswordHash());
            statement.setString(4, customer.getFullName());
            statement.setString(5, customer.getEmail());
            statement.executeUpdate();
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState()) || "23505".equals(exception.getSQLState())) {
                throw new DaoException("Tên đăng nhập hoặc email đã tồn tại");
            }
            throw new DaoException("Không thể tạo tài khoản", exception);
        }
    }

    @Override
    public void setActive(String userId, boolean active) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE users SET active=? WHERE user_id=?")) {
            statement.setBoolean(1, active);
            statement.setString(2, userId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DaoException("Không thể cập nhật trạng thái tài khoản", exception);
        }
    }

    private User map(ResultSet result) throws SQLException {
        String id = result.getString("user_id");
        String username = result.getString("username");
        String hash = result.getString("password_hash");
        String fullName = result.getString("full_name");
        String email = result.getString("email");
        boolean active = result.getBoolean("active");
        return switch (UserRole.valueOf(result.getString("role"))) {
            case CUSTOMER -> new Customer(id, username, hash, fullName, email, active);
            case STAFF -> new Staff(id, username, hash, fullName, email, active);
            case ADMIN -> new Admin(id, username, hash, fullName, email, active);
        };
    }
}
