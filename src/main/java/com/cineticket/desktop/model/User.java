package com.cineticket.desktop.model;

import java.util.Objects;

public abstract class User {
    private final String userId;
    private final String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private boolean active;
    private boolean loggedIn;

    protected User(String userId, String username, String passwordHash,
                   String fullName, String email, boolean active) {
        this.userId = requireText(userId, "userId");
        this.username = requireText(username, "username");
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.fullName = requireText(fullName, "fullName");
        this.email = requireText(email, "email");
        this.active = active;
    }

    public boolean login() {
        loggedIn = active;
        return loggedIn;
    }

    public void logout() {
        loggedIn = false;
    }

    public void updateProfile(String fullName, String email) {
        this.fullName = requireText(fullName, "fullName");
        this.email = requireText(email, "email");
    }

    public abstract UserRole getRole();

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " không được để trống");
        }
        return value.trim();
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public boolean isActive() { return active; }
    public boolean isLoggedIn() { return loggedIn; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = requireText(passwordHash, "passwordHash"); }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object object) {
        return object instanceof User other && userId.equals(other.userId);
    }

    @Override
    public int hashCode() { return Objects.hash(userId); }
}
