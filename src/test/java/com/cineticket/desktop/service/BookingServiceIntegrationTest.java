package com.cineticket.desktop.service;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.model.BookingStatus;
import com.cineticket.desktop.model.Customer;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceIntegrationTest {
    private final Customer customer = new Customer("customer-1", "customer", "x", "Test Customer", "test@example.com", true);

    @Test
    void concurrentRequestsCannotHoldTheSameSeatTwice() throws Exception {
        ConnectionFactory factory = TestDatabase.create();
        BookingService service = new BookingService(factory, Duration.ofMinutes(5));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        Callable<Void> task = () -> {
            start.await();
            try { service.createPendingBooking(customer, "show-1", List.of("ss-a1")); successes.incrementAndGet(); }
            catch (IllegalStateException ignored) { }
            return null;
        };
        Future<Void> first = executor.submit(task); Future<Void> second = executor.submit(task); start.countDown();
        first.get(10, TimeUnit.SECONDS); second.get(10, TimeUnit.SECONDS); executor.shutdownNow();

        assertEquals(1, successes.get());
        try (Connection connection = factory.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM bookings")) {
            result.next(); assertEquals(1, result.getInt(1));
        }
    }

    @Test
    void successfulMockPaymentIssuesTicketAndQrCode() throws Exception {
        ConnectionFactory factory = TestDatabase.create();
        BookingService service = new BookingService(factory, Duration.ofMinutes(5));
        BookingDetails pending = service.createPendingBooking(customer, "show-1", List.of("ss-a1", "ss-b1"));
        assertTrue(service.processPayment(pending.bookingId(), customer.getUserId(), "MOCK_SUCCESS"));
        BookingDetails confirmed = service.findById(pending.bookingId());
        assertEquals(BookingStatus.CONFIRMED, confirmed.status());
        assertEquals(2, confirmed.tickets().size());
        assertTrue(confirmed.tickets().stream().allMatch(ticket -> ticket.qrCode() != null));
    }

    @Test
    void failedMockPaymentIsKeptAsASeparateAttempt() throws Exception {
        ConnectionFactory factory = TestDatabase.create();
        BookingService service = new BookingService(factory, Duration.ofMinutes(5));
        BookingDetails pending = service.createPendingBooking(customer, "show-1", List.of("ss-a1"));
        assertFalse(service.processPayment(pending.bookingId(), customer.getUserId(), "MOCK_FAIL"));
        assertEquals(BookingStatus.PENDING, service.findById(pending.bookingId()).status());
        try (Connection connection = factory.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT status FROM payments")) {
            result.next(); assertEquals("FAILED", result.getString(1));
        }
    }
}
