package com.cineticket.desktop.service;

import com.cineticket.desktop.model.SeatType;
import com.cineticket.desktop.model.TicketStatus;

import java.math.BigDecimal;

public record TicketDetails(String ticketId, String ticketCode, String seatNumber, SeatType seatType,
                            BigDecimal price, TicketStatus status, String qrCode) {
}
