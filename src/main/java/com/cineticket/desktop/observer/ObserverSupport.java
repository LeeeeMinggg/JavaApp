package com.cineticket.desktop.observer;

import com.cineticket.desktop.model.NotificationObserver;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ObserverSupport {
    private final List<NotificationObserver> observers = new CopyOnWriteArrayList<>();

    public void subscribe(NotificationObserver observer) {
        if (observer != null) observers.add(observer);
    }

    public void unsubscribe(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String event) {
        observers.forEach(observer -> observer.update(event));
    }
}
