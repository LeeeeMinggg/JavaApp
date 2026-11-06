package com.cineticket.desktop.ui;

import javafx.scene.control.Alert;

public final class DialogUtils {
    private DialogUtils() { }
    public static void info(String title, String message) { show(Alert.AlertType.INFORMATION, title, message); }
    public static void error(String title, Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) message = throwable.getClass().getSimpleName();
        show(Alert.AlertType.ERROR, title, message);
    }
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        alert.setTitle(title); alert.setHeaderText(null);
        return alert.showAndWait().filter(type -> type.getButtonData().isDefaultButton()).isPresent();
    }
    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message); alert.setTitle(title); alert.setHeaderText(null); alert.showAndWait();
    }
}
