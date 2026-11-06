package com.cineticket.desktop.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingStrategyTest {
    private ShowtimeSeat showtimeSeat;

    @BeforeEach
    void setUp() {
        Movie movie = new Movie("m1", "Movie", "Description", "Action", 100, LocalDate.now(), MovieStatus.NOW_SHOWING, "");
        Cinema cinema = new Cinema("c1", "Cinema", "Address");
        Room room = new Room("r1", cinema, "Room 1", "ACTIVE");
        Seat seat = new Seat("s1", room, "A1", SeatType.STANDARD);
        Showtime showtime = new Showtime("st1", movie, room, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2), ShowtimeStatus.SCHEDULED, new BigDecimal("100000"));
        showtimeSeat = new ShowtimeSeat("ss1", showtime, seat, SeatStatus.AVAILABLE, null, null, 0);
    }

    @Test void standardPriceUsesBasePrice() { assertEquals(new BigDecimal("100000"), new StandardPricing().calculatePrice(showtimeSeat)); }
    @Test void vipPriceUsesFortyPercentSurcharge() { assertEquals(new BigDecimal("140000"), new VIPPricing().calculatePrice(showtimeSeat)); }
    @Test void couplePriceCoversTwoPeople() { assertEquals(new BigDecimal("200000"), new CouplePricing().calculatePrice(showtimeSeat)); }
}
