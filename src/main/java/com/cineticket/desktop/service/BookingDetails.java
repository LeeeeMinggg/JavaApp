package com.cineticket.desktop.service;

import com.cineticket.desktop.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetails(String bookingId, String customerId, String movieTitle,
                             String cinemaName, String roomName, LocalDateTime startTime,
                             LocalDateTime bookingTime, BigDecimal totalAmount, BookingStatus status,
                             LocalDateTime heldUntil, List<TicketDetails> tickets) {
}
