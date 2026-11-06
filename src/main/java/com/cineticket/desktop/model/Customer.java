package com.cineticket.desktop.model;

import java.util.ArrayList;
import java.util.List;

public final class Customer extends User {
    private final String customerId;
    private final List<Booking> bookingHistory = new ArrayList<>();

    public Customer(String userId, String username, String passwordHash,
                    String fullName, String email, boolean active) {
        super(userId, username, passwordHash, fullName, email, active);
        this.customerId = userId;
    }

    public List<Movie> searchMovies(String keyword, List<Movie> catalog) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase();
        return catalog.stream()
                .filter(Movie::isAvailable)
                .filter(movie -> movie.getTitle().toLowerCase().contains(query)
                        || movie.getGenre().toLowerCase().contains(query))
                .toList();
    }

    public List<Showtime> viewShowtimes(String movieId, List<Showtime> schedule) {
        return schedule.stream()
                .filter(showtime -> showtime.getMovie().getMovieId().equals(movieId))
                .toList();
    }

    public Booking bookTicket(Booking booking) {
        bookingHistory.add(booking);
        return booking;
    }

    public Payment payBooking(Booking booking, Payment payment) {
        booking.addPayment(payment);
        return payment;
    }

    public void cancelBooking(Booking booking) { booking.cancel(); }
    public List<Booking> viewBookingHistory() { return List.copyOf(bookingHistory); }
    public String getCustomerId() { return customerId; }
    @Override public UserRole getRole() { return UserRole.CUSTOMER; }
}
