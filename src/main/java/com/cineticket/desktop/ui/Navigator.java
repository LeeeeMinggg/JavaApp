package com.cineticket.desktop.ui;

import com.cineticket.desktop.CineTicketApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class Navigator {
    private static Stage stage;
    private Navigator() { }

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.setWidth(1280);
        stage.setHeight(800);
    }

    public static void show(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(CineTicketApplication.class.getResource("/com/cineticket/desktop/view/" + fxml));
            Parent root = loader.load();
            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root, 1280, 800);
                scene.getStylesheets().add(CineTicketApplication.class.getResource("/com/cineticket/desktop/styles/theme.css").toExternalForm());
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            stage.setTitle("CineTicket • " + title);
            stage.show();
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể mở giao diện " + fxml, exception);
        }
    }
}
