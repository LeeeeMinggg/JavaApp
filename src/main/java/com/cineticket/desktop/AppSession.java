package com.cineticket.desktop;

import com.cineticket.desktop.model.User;

public final class AppSession {
    private User currentUser;
    public User currentUser() {
        if (currentUser == null) throw new IllegalStateException("Chưa đăng nhập");
        return currentUser;
    }
    public User currentUserOrNull() { return currentUser; }
    public void login(User user) { currentUser = user; }
    public void logout() { if (currentUser != null) currentUser.logout(); currentUser = null; }
}
