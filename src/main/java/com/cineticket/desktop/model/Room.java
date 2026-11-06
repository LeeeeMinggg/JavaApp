package com.cineticket.desktop.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Room {
    private final String roomId;
    private final Cinema cinema;
    private String roomName;
    private String status;
    private final List<Seat> seats = new ArrayList<>();

    public Room(String roomId, Cinema cinema, String roomName, String status) {
        this.roomId = Objects.requireNonNull(roomId);
        this.cinema = Objects.requireNonNull(cinema);
        updateRoom(roomName, status);
    }

    public void updateRoom(String roomName, String status) {
        if (roomName == null || roomName.isBlank()) throw new IllegalArgumentException("Tên phòng không hợp lệ");
        this.roomName = roomName.trim();
        this.status = status == null ? "ACTIVE" : status;
    }
    public boolean checkAvailability() { return "ACTIVE".equalsIgnoreCase(status); }
    public void addSeat(Seat seat) { seats.add(Objects.requireNonNull(seat)); }
    public List<Seat> getSeats() { return List.copyOf(seats); }
    public String getRoomId() { return roomId; }
    public Cinema getCinema() { return cinema; }
    public String getRoomName() { return roomName; }
    public String getStatus() { return status; }
    @Override public String toString() { return cinema.getName() + " • " + roomName; }
}
