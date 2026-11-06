package com.cineticket.desktop.model;

import com.cineticket.desktop.observer.ObserverSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Showtime implements NotificationSubject {
    private final String showtimeId;
    private final Movie movie;
    private final Room room;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ShowtimeStatus status;
    private BigDecimal basePrice;
    private final List<ShowtimeSeat> showtimeSeats = new ArrayList<>();
    private final ObserverSupport observers = new ObserverSupport();

    public Showtime(String showtimeId, Movie movie, Room room, LocalDateTime startTime,
                    LocalDateTime endTime, ShowtimeStatus status, BigDecimal basePrice) {
        this.showtimeId = Objects.requireNonNull(showtimeId);
        this.movie = Objects.requireNonNull(movie);
        this.room = Objects.requireNonNull(room);
        this.basePrice = requirePositive(basePrice);
        updateShowtime(startTime, endTime, status);
    }

    public Showtime createShowtime() { return this; }

    public void updateShowtime(LocalDateTime startTime, LocalDateTime endTime, ShowtimeStatus status) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời gian suất chiếu không hợp lệ");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = Objects.requireNonNull(status);
        notifyObservers("SHOWTIME_CHANGED:" + showtimeId);
    }

    public void cancel() {
        status = ShowtimeStatus.CANCELLED;
        notifyObservers("SHOWTIME_CHANGED:" + showtimeId);
    }

    public String getDetails() {
        return movie.getTitle() + " • " + room + " • " + startTime;
    }

    public void addShowtimeSeat(ShowtimeSeat seat) { showtimeSeats.add(Objects.requireNonNull(seat)); }
    private BigDecimal requirePositive(BigDecimal value) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("Giá cơ bản phải lớn hơn 0");
        return value;
    }

    @Override public void subscribe(NotificationObserver observer) { observers.subscribe(observer); }
    @Override public void unsubscribe(NotificationObserver observer) { observers.unsubscribe(observer); }
    @Override public void notifyObservers(String event) { observers.notifyObservers(event); }
    public String getShowtimeId() { return showtimeId; }
    public Movie getMovie() { return movie; }
    public Room getRoom() { return room; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public ShowtimeStatus getStatus() { return status; }
    public BigDecimal getBasePrice() { return basePrice; }
    public List<ShowtimeSeat> getShowtimeSeats() { return List.copyOf(showtimeSeats); }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = requirePositive(basePrice); }
    @Override public String toString() { return getDetails(); }
}
