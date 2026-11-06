package com.cineticket.desktop.model;

public interface NotificationSubject {
    void subscribe(NotificationObserver observer);
    void unsubscribe(NotificationObserver observer);
    void notifyObservers(String event);
}
