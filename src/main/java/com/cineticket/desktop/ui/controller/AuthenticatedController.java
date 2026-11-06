package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.User;
import com.cineticket.desktop.model.UserRole;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class AuthenticatedController {
    @FXML protected Label userLabel;

    protected User currentUser() { return AppContext.get().session().currentUser(); }

    protected void initializeHeader() {
        if (userLabel != null) userLabel.setText(currentUser().getFullName() + " • " + currentUser().getRole());
    }

    @FXML public void goHome() {
        UserRole role = currentUser().getRole();
        if (role == UserRole.CUSTOMER) Navigator.show("home.fxml", "Phim đang chiếu");
        else if (role == UserRole.STAFF) Navigator.show("staff-dashboard.fxml", "Quản lý vận hành");
        else Navigator.show("admin-dashboard.fxml", "Quản trị hệ thống");
    }

    @FXML public void goHistory() { Navigator.show("booking-history.fxml", "Vé của tôi"); }

    @FXML public void logout() {
        AppContext.get().session().logout();
        AppContext.get().viewState().clear();
        Navigator.show("login.fxml", "Đăng nhập");
    }
}
