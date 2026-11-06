package com.cineticket.desktop.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public final class ShowtimeSeat {
    private final String showtimeSeatId;
    private final Showtime showtime;
    private final Seat seat;
    private SeatStatus status;
    private LocalDateTime heldUntil;
    private BigDecimal price;
    private int version;

    public ShowtimeSeat(String showtimeSeatId, Showtime showtime, Seat seat, SeatStatus status,
                        LocalDateTime heldUntil, BigDecimal price, int version) {
        this.showtimeSeatId = Objects.requireNonNull(showtimeSeatId);
        this.showtime = Objects.requireNonNull(showtime);
        this.seat = Objects.requireNonNull(seat);
        this.status = Objects.requireNonNull(status);
        this.heldUntil = heldUntil;
        this.price = price == null ? showtime.getBasePrice() : price;
        this.version = version;
    }

    public synchronized boolean isAvailable() {
        if (status == SeatStatus.HELD && heldUntil != null && !heldUntil.isAfter(LocalDateTime.now())) {
            releaseSeat();
        }
        return status == SeatStatus.AVAILABLE;
    }

    public synchronized boolean holdSeat(Duration duration) {
        if (!isAvailable()) return false;
        status = SeatStatus.HELD;
        heldUntil = LocalDateTime.now().plus(duration);
        version++;
        return true;
    }

    public synchronized void confirmSeat() {
        if (status != SeatStatus.HELD) throw new IllegalStateException("Chỉ ghế đang giữ mới được xác nhận");
        status = SeatStatus.CONFIRMED;
        heldUntil = null;
        version++;
    }

    public synchronized void releaseSeat() {
        status = SeatStatus.AVAILABLE;
        heldUntil = null;
        version++;
    }

    public void applyPricing(PricingStrategy strategy) { this.price = strategy.calculatePrice(this); }
    public String getShowtimeSeatId() { return showtimeSeatId; }
    public Showtime getShowtime() { return showtime; }
    public Seat getSeat() { return seat; }
    public SeatStatus getStatus() { return status; }
    public LocalDateTime getHeldUntil() { return heldUntil; }
    public BigDecimal getPrice() { return price; }
    public int getVersion() { return version; }
    public BigDecimal getBasePrice() { return showtime.getBasePrice(); }
}
