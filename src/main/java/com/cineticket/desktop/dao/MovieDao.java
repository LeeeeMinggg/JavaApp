package com.cineticket.desktop.dao;

import com.cineticket.desktop.model.Movie;

import java.util.List;
import java.util.Optional;

public interface MovieDao {
    List<Movie> findNowShowing(String keyword);
    List<Movie> findAll();
    Optional<Movie> findById(String movieId);
    void save(Movie movie);
    void update(Movie movie);
}
