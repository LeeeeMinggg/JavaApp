package com.cineticket.desktop.model;

import java.util.Objects;

public final class Seat {
    private final String seatId;
    private final Room room;
    private final String seatNumber;
    private final SeatType type;

    public Seat(String seatId, Room room, String seatNumber, SeatType type) {
        this.seatId = Objects.requireNonNull(seatId);
        this.room = Objects.requireNonNull(room);
        this.seatNumber = Objects.requireNonNull(seatNumber);
        this.type = Objects.requireNonNull(type);
    }

    public String getDetails() { return seatNumber + " (" + type + ")"; }
    public String getSeatId() { return seatId; }
    public Room getRoom() { return room; }
    public String getSeatNumber() { return seatNumber; }
    public SeatType getType() { return type; }
    @Override public String toString() { return getDetails(); }
}
