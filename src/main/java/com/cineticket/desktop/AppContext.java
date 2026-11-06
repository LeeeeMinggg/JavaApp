package com.cineticket.desktop;

import com.cineticket.desktop.config.*;
import com.cineticket.desktop.dao.*;
import com.cineticket.desktop.service.*;

import java.sql.Connection;
import java.sql.SQLException;

public final class AppContext {
    private static final AppContext INSTANCE = new AppContext();

    private final AppConfig config;
    private final ConnectionFactory connectionFactory;
    private final UserDao userDao;
    private final MovieDao movieDao;
    private final ShowtimeDao showtimeDao;
    private final AuthService authService;
    private final BookingService bookingService;
    private final AdminService adminService;
    private final ManagementService managementService;
    private final AppSession session = new AppSession();
    private final ViewState viewState = new ViewState();

    private AppContext() {
        config = AppConfig.load();
        connectionFactory = new DriverManagerConnectionFactory(config.databaseUrl(), config.databaseUser(), config.databasePassword());
        new DatabaseInitializer(connectionFactory).initialize();
        userDao = new JdbcUserDao(connectionFactory);
        movieDao = new JdbcMovieDao(connectionFactory);
        showtimeDao = new JdbcShowtimeDao(connectionFactory);
        authService = new AuthService(userDao, new PasswordHasher());
        bookingService = new BookingService(connectionFactory, config.seatHoldDuration());
        adminService = new AdminService(connectionFactory, userDao);
        managementService = new ManagementService(connectionFactory, movieDao, showtimeDao);
    }

    public static AppContext get() { return INSTANCE; }
    public boolean databaseAvailable() {
        try (Connection ignored = connectionFactory.getConnection()) { return true; }
        catch (SQLException exception) { return false; }
    }
    public AppConfig config() { return config; }
    public UserDao userDao() { return userDao; }
    public MovieDao movieDao() { return movieDao; }
    public ShowtimeDao showtimeDao() { return showtimeDao; }
    public AuthService authService() { return authService; }
    public BookingService bookingService() { return bookingService; }
    public AdminService adminService() { return adminService; }
    public ManagementService managementService() { return managementService; }
    public AppSession session() { return session; }
    public ViewState viewState() { return viewState; }
}
