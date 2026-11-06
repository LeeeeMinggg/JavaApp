package com.cineticket.desktop.observer;

import com.cineticket.desktop.model.NotificationObserver;

public final class ConsoleNotificationObserver implements NotificationObserver {
    @Override
    public void update(String event) {
        System.out.println("[CineTicket event] " + event);
    }
}
