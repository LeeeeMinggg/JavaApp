package com.cineticket.desktop.dao;

import com.cineticket.desktop.model.SeatStatus;
import com.cineticket.desktop.model.SeatType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SeatView(String showtimeSeatId, String seatId, String seatNumber, SeatType seatType,
                       SeatStatus status, LocalDateTime heldUntil, BigDecimal price, int version) {
    public boolean selectable(LocalDateTime now) {
        return status == SeatStatus.AVAILABLE
                || (status == SeatStatus.HELD && heldUntil != null && !heldUntil.isAfter(now));
    }
}
