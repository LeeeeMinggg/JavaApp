package com.cineticket.desktop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Cinema {
    private final String cinemaId;
    private String name;
    private String address;
    private final List<Room> rooms = new ArrayList<>();

    public Cinema(String cinemaId, String name, String address) {
        this.cinemaId = Objects.requireNonNull(cinemaId);
        updateCinemaInfo(name, address);
    }

    public String getCinemaInfo() { return name + " - " + address; }
    public void updateCinemaInfo(String name, String address) {
        if (name == null || name.isBlank() || address == null || address.isBlank()) {
            throw new IllegalArgumentException("Thông tin rạp không hợp lệ");
        }
        this.name = name.trim();
        this.address = address.trim();
    }
    public void addRoom(Room room) { rooms.add(Objects.requireNonNull(room)); }
    public String getCinemaId() { return cinemaId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public List<Room> getRooms() { return List.copyOf(rooms); }
    @Override public String toString() { return name; }
}
