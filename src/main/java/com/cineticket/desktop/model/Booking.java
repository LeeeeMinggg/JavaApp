package com.cineticket.desktop.model;

import com.cineticket.desktop.observer.ObserverSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Booking implements NotificationSubject {
    private final String bookingId;
    private final Customer customer;
    private final Showtime showtime;
    private final LocalDateTime bookingTime;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Payment> payments = new ArrayList<>();
    private final ObserverSupport observers = new ObserverSupport();

    public Booking(String bookingId, Customer customer, Showtime showtime,
                   LocalDateTime bookingTime, BookingStatus status) {
        this.bookingId = Objects.requireNonNull(bookingId);
        this.customer = Objects.requireNonNull(customer);
        this.showtime = Objects.requireNonNull(showtime);
        this.bookingTime = Objects.requireNonNull(bookingTime);
        this.status = Objects.requireNonNull(status);
        this.totalAmount = BigDecimal.ZERO;
    }

    public Booking createBooking() {
        if (tickets.isEmpty()) throw new IllegalStateException("Booking phải có ít nhất một vé");
        totalAmount = calculateTotal();
        return this;
    }

    public BigDecimal calculateTotal() {
        return tickets.stream().map(Ticket::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirm() {
        boolean paid = payments.stream().anyMatch(payment -> payment.getStatus() == PaymentStatus.SUCCESS
                && payment.getAmount().compareTo(totalAmount) == 0);
        if (!paid) throw new IllegalStateException("Không thể xác nhận booking khi chưa thanh toán thành công");
        tickets.forEach(ticket -> {
            ticket.getShowtimeSeat().confirmSeat();
            ticket.issue();
        });
        status = BookingStatus.CONFIRMED;
        notifyObservers("BOOKING_CONFIRMED:" + bookingId);
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) return;
        payments.stream().filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS).forEach(Payment::refund);
        tickets.forEach(ticket -> {
            ticket.cancel();
            ticket.getShowtimeSeat().releaseSeat();
        });
        status = BookingStatus.CANCELLED;
        notifyObservers("BOOKING_CANCELLED:" + bookingId);
    }

    public void addTicket(Ticket ticket) { tickets.add(Objects.requireNonNull(ticket)); totalAmount = calculateTotal(); }
    public void addPayment(Payment payment) { payments.add(Objects.requireNonNull(payment)); }
    @Override public void subscribe(NotificationObserver observer) { observers.subscribe(observer); }
    @Override public void unsubscribe(NotificationObserver observer) { observers.unsubscribe(observer); }
    @Override public void notifyObservers(String event) { observers.notifyObservers(event); }
    public String getBookingId() { return bookingId; }
    public Customer getCustomer() { return customer; }
    public Showtime getShowtime() { return showtime; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BookingStatus getStatus() { return status; }
    public List<Ticket> getTickets() { return List.copyOf(tickets); }
    public List<Payment> getPayments() { return List.copyOf(payments); }
}
