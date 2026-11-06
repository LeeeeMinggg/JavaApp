package com.cineticket.desktop;

import com.cineticket.desktop.ui.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

public final class CineTicketApplication extends Application {
    @Override
    public void start(Stage stage) {
        Navigator.initialize(stage);
        Navigator.show("login.fxml", "Đăng nhập");
    }

    public static void main(String[] args) { launch(args); }
}
