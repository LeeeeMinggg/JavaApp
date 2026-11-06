package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.User;
import com.cineticket.desktop.model.UserRole;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label databaseStatusLabel;

    @FXML
    private void initialize() {
        boolean available = AppContext.get().databaseAvailable();
        databaseStatusLabel.setText(available ? "● Database H2 sẵn sàng" : "● Không thể khởi tạo database H2");
        databaseStatusLabel.getStyleClass().add(available ? "status-online" : "status-offline");
        usernameField.setText("customer");
        passwordField.setText("customer123");
    }

    @FXML private void login() {
        try {
            User user = AppContext.get().authService().login(usernameField.getText(), passwordField.getText());
            AppContext.get().session().login(user);
            if (user.getRole() == UserRole.CUSTOMER) Navigator.show("home.fxml", "Phim đang chiếu");
            else if (user.getRole() == UserRole.STAFF) Navigator.show("staff-dashboard.fxml", "Quản lý vận hành");
            else Navigator.show("admin-dashboard.fxml", "Quản trị hệ thống");
        } catch (RuntimeException exception) { DialogUtils.error("Đăng nhập không thành công", exception); }
    }

    @FXML private void openRegister() { Navigator.show("register.fxml", "Tạo tài khoản"); }
}
