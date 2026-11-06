package com.cineticket.desktop.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueRow(LocalDate date, long tickets, long bookings, BigDecimal revenue) { }
