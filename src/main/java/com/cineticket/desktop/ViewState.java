package com.cineticket.desktop;

import com.cineticket.desktop.model.Movie;
import com.cineticket.desktop.model.Showtime;
import com.cineticket.desktop.service.BookingDetails;

public final class ViewState {
    private Movie selectedMovie;
    private Showtime selectedShowtime;
    private BookingDetails selectedBooking;
    public Movie selectedMovie() { return selectedMovie; }
    public void selectedMovie(Movie selectedMovie) { this.selectedMovie = selectedMovie; }
    public Showtime selectedShowtime() { return selectedShowtime; }
    public void selectedShowtime(Showtime selectedShowtime) { this.selectedShowtime = selectedShowtime; }
    public BookingDetails selectedBooking() { return selectedBooking; }
    public void selectedBooking(BookingDetails selectedBooking) { this.selectedBooking = selectedBooking; }
    public void clear() { selectedMovie = null; selectedShowtime = null; selectedBooking = null; }
}
