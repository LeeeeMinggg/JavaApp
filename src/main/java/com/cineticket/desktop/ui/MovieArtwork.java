package com.cineticket.desktop.ui;

import com.cineticket.desktop.CineTicketApplication;
import com.cineticket.desktop.model.Movie;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.Map;

public final class MovieArtwork {
    private static final Map<String, String> POSTERS = Map.of(
            "movie-01", "poster-dune.jpg",
            "movie-02", "poster-mai.jpg",
            "movie-03", "poster-dao-pho-piano.jpg",
            "movie-04", "poster-kungfu.jpg",
            "movie-05", "poster-godzilla.jpg",
            "movie-06", "poster-insideout.jpg"
    );

    private MovieArtwork() { }

    public static Image poster(Movie movie) {
        return load(POSTERS.getOrDefault(movie.getMovieId(), "poster-dune.jpg"));
    }

    public static Image hero() { return load("hero-dune.jpg"); }

    private static Image load(String fileName) {
        URL resource = CineTicketApplication.class.getResource("/com/cineticket/desktop/images/" + fileName);
        if (resource == null) throw new IllegalStateException("Thiếu ảnh giao diện: " + fileName);
        return new Image(resource.toExternalForm(), true);
    }
}
