package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.Customer;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public final class RegisterController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private void register() {
        try {
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
            }
            Customer customer = AppContext.get().authService().register(usernameField.getText(), passwordField.getText(),
                    fullNameField.getText(), emailField.getText());
            AppContext.get().session().login(customer);
            Navigator.show("home.fxml", "Phim đang chiếu");
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tạo tài khoản", exception); }
    }

    @FXML private void backToLogin() { Navigator.show("login.fxml", "Đăng nhập"); }
}
