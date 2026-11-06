package com.cineticket.desktop.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingDomainTest {
    @Test
    void successfulPaymentConfirmsBookingAndNotifiesObserver() {
        Customer customer = new Customer("u1", "user1", "hash", "User One", "u1@example.com", true);
        Movie movie = new Movie("m1", "Movie", "Description", "Action", 100, LocalDate.now(), MovieStatus.NOW_SHOWING, "");
        Cinema cinema = new Cinema("c1", "Cinema", "Address");
        Room room = new Room("r1", cinema, "Room", "ACTIVE");
        Seat seat = new Seat("s1", room, "A1", SeatType.STANDARD);
        Showtime showtime = new Showtime("st1", movie, room, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2), ShowtimeStatus.SCHEDULED, new BigDecimal("90000"));
        ShowtimeSeat showtimeSeat = new ShowtimeSeat("ss1", showtime, seat, SeatStatus.AVAILABLE, null, new BigDecimal("90000"), 0);
        assertTrue(showtimeSeat.holdSeat(Duration.ofMinutes(5)));

        Booking booking = new Booking("b1", customer, showtime, LocalDateTime.now(), BookingStatus.PENDING);
        booking.addTicket(new Ticket("t1", "CT-TEST", showtimeSeat, new BigDecimal("90000"), TicketStatus.PENDING));
        booking.createBooking();
        List<String> events = new ArrayList<>(); booking.subscribe(events::add);
        Payment payment = new Payment("p1", new BigDecimal("90000"), LocalDateTime.now(), "MOCK_SUCCESS",
                PaymentStatus.PENDING, new PaymentGateway("g1", "Mock", "ACTIVE"));
        assertTrue(payment.processPayment());
        booking.addPayment(payment); booking.confirm();

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(SeatStatus.CONFIRMED, showtimeSeat.getStatus());
        assertEquals(TicketStatus.ISSUED, booking.getTickets().get(0).getStatus());
        assertTrue(events.get(0).startsWith("BOOKING_CONFIRMED"));
    }

    @Test
    void bookingCannotBeConfirmedWithoutSuccessfulPayment() {
        Customer customer = new Customer("u1", "user1", "hash", "User One", "u1@example.com", true);
        Movie movie = new Movie("m1", "Movie", "Description", "Action", 100, LocalDate.now(), MovieStatus.NOW_SHOWING, "");
        Cinema cinema = new Cinema("c1", "Cinema", "Address"); Room room = new Room("r1", cinema, "Room", "ACTIVE");
        Showtime showtime = new Showtime("st1", movie, room, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), ShowtimeStatus.SCHEDULED, BigDecimal.TEN);
        ShowtimeSeat seat = new ShowtimeSeat("ss1", showtime, new Seat("s1", room, "A1", SeatType.STANDARD), SeatStatus.AVAILABLE, null, BigDecimal.TEN, 0);
        seat.holdSeat(Duration.ofMinutes(5));
        Booking booking = new Booking("b1", customer, showtime, LocalDateTime.now(), BookingStatus.PENDING);
        booking.addTicket(new Ticket("t1", "CT-1", seat, BigDecimal.TEN, TicketStatus.PENDING)); booking.createBooking();
        assertThrows(IllegalStateException.class, booking::confirm);
    }
}
