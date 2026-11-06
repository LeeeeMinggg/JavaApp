package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.Movie;
import com.cineticket.desktop.model.MovieStatus;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.MovieArtwork;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public final class HomeController extends AuthenticatedController {
    @FXML private TextField searchField;
    @FXML private HBox movieRow;
    @FXML private HBox comingSoonRow;
    @FXML private Label resultLabel;
    @FXML private ImageView heroImage;
    @FXML private ScrollPane pageScroll;
    @FXML private VBox movieSection;

    @FXML private void initialize() {
        initializeHeader();
        heroImage.setImage(MovieArtwork.hero());
        heroImage.fitWidthProperty().bind(pageScroll.widthProperty());
        loadMovies();
    }

    @FXML private void search() { loadMovies(); focusMovies(); }
    @FXML private void focusMovies() { pageScroll.setVvalue(0.58); }

    @FXML private void openHeroMovie() {
        AppContext.get().movieDao().findById("movie-01").ifPresent(this::openMovie);
    }

    private void loadMovies() {
        try {
            String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
            List<Movie> nowShowing = AppContext.get().movieDao().findNowShowing(keyword);
            movieRow.getChildren().setAll(nowShowing.stream().map(this::createMovieCard).toList());
            List<Movie> comingSoon = AppContext.get().movieDao().findAll().stream()
                    .filter(movie -> movie.getStatus() == MovieStatus.COMING_SOON)
                    .filter(movie -> keyword.isBlank() || movie.getTitle().toLowerCase().contains(keyword.toLowerCase())
                            || movie.getGenre().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
            comingSoonRow.getChildren().setAll(comingSoon.stream().map(this::createMovieCard).toList());
            resultLabel.setText(nowShowing.size() + " phim đang mở bán hôm nay");
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tải phim", exception); }
    }

    private VBox createMovieCard(Movie movie) {
        ImageView poster = new ImageView(MovieArtwork.poster(movie));
        poster.setFitWidth(205); poster.setFitHeight(295); poster.setPreserveRatio(false);
        Label title = new Label(movie.getTitle()); title.getStyleClass().add("poster-title"); title.setWrapText(true);
        Label meta = new Label(movie.getGenre() + "  •  " + movie.getDuration() + " phút");
        meta.getStyleClass().add("poster-meta");
        Button buy = new Button(movie.isAvailable() ? "Mua vé" : "Chi tiết");
        buy.getStyleClass().add("mini-ticket-button"); buy.setOnAction(event -> openMovie(movie));
        Button details = new Button("Xem thêm  ›"); details.getStyleClass().add("mini-detail-button");
        details.setOnAction(event -> openMovie(movie));
        Region spacer = new Region(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        HBox actions = new HBox(8, buy, spacer, details); actions.setAlignment(Pos.CENTER_LEFT);
        VBox card = new VBox(9, poster, title, meta, actions);
        card.getStyleClass().add("cinema-movie-card"); card.setPrefWidth(229); card.setMinWidth(229);
        return card;
    }

    private void openMovie(Movie movie) {
        AppContext.get().viewState().selectedMovie(movie);
        Navigator.show("movie-details.fxml", movie.getTitle());
    }
}
