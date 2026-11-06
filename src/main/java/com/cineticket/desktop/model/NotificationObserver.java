package com.cineticket.desktop.model;

@FunctionalInterface
public interface NotificationObserver {
    void update(String event);
}
