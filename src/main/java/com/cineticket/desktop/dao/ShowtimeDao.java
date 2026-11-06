package com.cineticket.desktop.dao;

import com.cineticket.desktop.model.Showtime;

import java.util.List;
import java.util.Optional;

public interface ShowtimeDao {
    List<Showtime> findByMovie(String movieId);
    List<Showtime> findFuture();
    Optional<Showtime> findById(String showtimeId);
    List<SeatView> findSeats(String showtimeId);
    void save(Showtime showtime);
}
