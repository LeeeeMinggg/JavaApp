package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.Movie;
import com.cineticket.desktop.model.Showtime;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Formatters;
import com.cineticket.desktop.ui.MovieArtwork;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class MovieDetailsController extends AuthenticatedController {
    @FXML private ImageView posterImage;
    @FXML private Label titleLabel;
    @FXML private Label genreLabel;
    @FXML private Label durationLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox showtimeList;

    @FXML private void initialize() {
        initializeHeader();
        Movie movie = AppContext.get().viewState().selectedMovie();
        if (movie == null) { goHome(); return; }
        posterImage.setImage(MovieArtwork.poster(movie));
        titleLabel.setText(movie.getTitle()); genreLabel.setText(movie.getGenre());
        durationLabel.setText(movie.getDuration() + " phút • Khởi chiếu " + movie.getReleaseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        descriptionLabel.setText(movie.getDescription());
        loadShowtimes(movie);
    }

    private void loadShowtimes(Movie movie) {
        try {
            List<Showtime> showtimes = AppContext.get().showtimeDao().findByMovie(movie.getMovieId());
            showtimeList.getChildren().clear();
            if (showtimes.isEmpty()) showtimeList.getChildren().add(new Label("Chưa có suất chiếu mở bán."));
            for (Showtime showtime : showtimes) {
                Button button = new Button(Formatters.dateTime(showtime.getStartTime()) + "   •   " + showtime.getRoom());
                button.getStyleClass().add("showtime-button"); button.setMaxWidth(Double.MAX_VALUE);
                button.setOnAction(event -> {
                    AppContext.get().viewState().selectedShowtime(showtime);
                    Navigator.show("seat-selection.fxml", "Chọn ghế");
                });
                showtimeList.getChildren().add(button);
            }
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tải lịch chiếu", exception); }
    }

    @FXML private void back() { Navigator.show("home.fxml", "Phim đang chiếu"); }
}
