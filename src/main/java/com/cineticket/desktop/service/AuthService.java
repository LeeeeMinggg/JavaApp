package com.cineticket.desktop.service;

import com.cineticket.desktop.dao.DaoException;
import com.cineticket.desktop.dao.UserDao;
import com.cineticket.desktop.model.Customer;
import com.cineticket.desktop.model.User;

import java.util.UUID;

public final class AuthService {
    private final UserDao userDao;
    private final PasswordHasher passwordHasher;

    public AuthService(UserDao userDao, PasswordHasher passwordHasher) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
    }

    public User login(String username, String password) {
        User user = userDao.findByUsername(username == null ? "" : username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu"));
        if (!user.isActive()) throw new IllegalStateException("Tài khoản đã bị khóa");
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu");
        }
        user.login();
        return user;
    }

    public Customer register(String username, String password, String fullName, String email) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (!normalizedUsername.matches("[A-Za-z0-9_]{4,30}")) {
            throw new IllegalArgumentException("Tên đăng nhập cần 4-30 ký tự, chỉ gồm chữ, số và _");
        }
        if (!normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }
        Customer customer = new Customer(UUID.randomUUID().toString(), normalizedUsername,
                passwordHasher.hash(password), fullName, normalizedEmail, true);
        userDao.createCustomer(customer);
        return customer;
    }
}
