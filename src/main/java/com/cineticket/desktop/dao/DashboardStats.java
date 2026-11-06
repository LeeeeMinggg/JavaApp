package com.cineticket.desktop.dao;

import java.math.BigDecimal;

public record DashboardStats(long totalUsers, long activeMovies, long futureShowtimes,
                             long confirmedBookings, long issuedTickets, BigDecimal totalRevenue) {
}
