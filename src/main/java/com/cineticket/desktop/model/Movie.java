package com.cineticket.desktop.model;

import java.time.LocalDate;
import java.util.Objects;

public final class Movie {
    private final String movieId;
    private String title;
    private String description;
    private String genre;
    private int duration;
    private LocalDate releaseDate;
    private MovieStatus status;
    private String posterUrl;

    public Movie(String movieId, String title, String description, String genre, int duration,
                 LocalDate releaseDate, MovieStatus status, String posterUrl) {
        this.movieId = Objects.requireNonNull(movieId);
        updateMovie(title, description, genre, duration, releaseDate, status, posterUrl);
    }

    public String getDetails() {
        return title + " • " + genre + " • " + duration + " phút";
    }

    public void updateMovie(String title, String description, String genre, int duration,
                            LocalDate releaseDate, MovieStatus status, String posterUrl) {
        if (title == null || title.isBlank() || duration <= 0) {
            throw new IllegalArgumentException("Tên phim và thời lượng phải hợp lệ");
        }
        this.title = title.trim();
        this.description = description == null ? "" : description.trim();
        this.genre = genre == null ? "Khác" : genre.trim();
        this.duration = duration;
        this.releaseDate = Objects.requireNonNull(releaseDate);
        this.status = Objects.requireNonNull(status);
        this.posterUrl = posterUrl == null ? "" : posterUrl.trim();
    }

    public boolean isAvailable() { return status == MovieStatus.NOW_SHOWING; }
    public String getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public MovieStatus getStatus() { return status; }
    public String getPosterUrl() { return posterUrl; }
    @Override public String toString() { return title; }
}
