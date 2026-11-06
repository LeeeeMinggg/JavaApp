package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.*;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Formatters;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public final class StaffDashboardController extends AuthenticatedController {
    @FXML private TableView<Movie> movieTable;
    @FXML private TextField movieTitleField;
    @FXML private TextField movieGenreField;
    @FXML private TextField movieDurationField;
    @FXML private TextField moviePosterField;
    @FXML private TextArea movieDescriptionArea;
    @FXML private DatePicker movieReleaseDatePicker;
    @FXML private ComboBox<MovieStatus> movieStatusBox;
    @FXML private Label movieCountLabel;

    @FXML private TableView<Showtime> showtimeTable;
    @FXML private ComboBox<Movie> showtimeMovieBox;
    @FXML private ComboBox<Room> showtimeRoomBox;
    @FXML private DatePicker showtimeDatePicker;
    @FXML private TextField showtimeTimeField;
    @FXML private TextField showtimePriceField;
    @FXML private Label roomCountLabel;

    @FXML private void initialize() {
        initializeHeader(); configureTables();
        movieStatusBox.getItems().setAll(MovieStatus.values()); movieStatusBox.setValue(MovieStatus.NOW_SHOWING);
        movieReleaseDatePicker.setValue(LocalDate.now()); showtimeDatePicker.setValue(LocalDate.now().plusDays(1));
        showtimeTimeField.setText("19:30"); showtimePriceField.setText("90000");
        loadData();
        movieTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, movie) -> fillMovieForm(movie));
    }

    private void configureTables() {
        TableColumn<Movie, String> movieTitle = new TableColumn<>("Tên phim");
        movieTitle.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getTitle())); movieTitle.setPrefWidth(230);
        TableColumn<Movie, String> genre = new TableColumn<>("Thể loại");
        genre.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getGenre())); genre.setPrefWidth(170);
        TableColumn<Movie, String> duration = new TableColumn<>("Thời lượng");
        duration.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getDuration() + " phút")); duration.setPrefWidth(100);
        TableColumn<Movie, String> status = new TableColumn<>("Trạng thái");
        status.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getStatus().name())); status.setPrefWidth(130);
        movieTable.getColumns().setAll(movieTitle, genre, duration, status);

        TableColumn<Showtime, String> showMovie = new TableColumn<>("Phim");
        showMovie.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getMovie().getTitle())); showMovie.setPrefWidth(210);
        TableColumn<Showtime, String> showRoom = new TableColumn<>("Rạp / phòng");
        showRoom.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getRoom().toString())); showRoom.setPrefWidth(250);
        TableColumn<Showtime, String> showTime = new TableColumn<>("Thời gian");
        showTime.setCellValueFactory(row -> new SimpleStringProperty(Formatters.dateTime(row.getValue().getStartTime()))); showTime.setPrefWidth(160);
        TableColumn<Showtime, String> showPrice = new TableColumn<>("Giá chuẩn");
        showPrice.setCellValueFactory(row -> new SimpleStringProperty(Formatters.money(row.getValue().getBasePrice()))); showPrice.setPrefWidth(120);
        showtimeTable.getColumns().setAll(showMovie, showRoom, showTime, showPrice);
    }

    private void loadData() {
        try {
            List<Movie> movies = AppContext.get().movieDao().findAll();
            List<Room> rooms = AppContext.get().managementService().rooms();
            movieTable.getItems().setAll(movies); showtimeMovieBox.getItems().setAll(movies);
            showtimeRoomBox.getItems().setAll(rooms); showtimeTable.getItems().setAll(AppContext.get().showtimeDao().findFuture());
            movieCountLabel.setText(movies.size() + " phim"); roomCountLabel.setText(rooms.size() + " phòng đang hoạt động");
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tải dữ liệu vận hành", exception); }
    }

    private void fillMovieForm(Movie movie) {
        if (movie == null) return;
        movieTitleField.setText(movie.getTitle()); movieGenreField.setText(movie.getGenre());
        movieDurationField.setText(String.valueOf(movie.getDuration())); moviePosterField.setText(movie.getPosterUrl());
        movieDescriptionArea.setText(movie.getDescription()); movieReleaseDatePicker.setValue(movie.getReleaseDate());
        movieStatusBox.setValue(movie.getStatus());
    }

    @FXML private void newMovie() {
        movieTable.getSelectionModel().clearSelection(); movieTitleField.clear(); movieGenreField.clear();
        movieDurationField.clear(); moviePosterField.clear(); movieDescriptionArea.clear();
        movieReleaseDatePicker.setValue(LocalDate.now()); movieStatusBox.setValue(MovieStatus.NOW_SHOWING);
    }

    @FXML private void saveMovie() {
        try {
            Movie selected = movieTable.getSelectionModel().getSelectedItem();
            String id = selected == null ? UUID.randomUUID().toString() : selected.getMovieId();
            Movie movie = new Movie(id, movieTitleField.getText(), movieDescriptionArea.getText(), movieGenreField.getText(),
                    Integer.parseInt(movieDurationField.getText()), movieReleaseDatePicker.getValue(), movieStatusBox.getValue(), moviePosterField.getText());
            AppContext.get().managementService().saveMovie(movie, selected != null);
            DialogUtils.info("Đã lưu phim", movie.getTitle()); loadData(); newMovie();
        } catch (RuntimeException exception) { DialogUtils.error("Không thể lưu phim", exception); }
    }

    @FXML private void createShowtime() {
        try {
            Movie movie = showtimeMovieBox.getValue(); Room room = showtimeRoomBox.getValue();
            if (movie == null || room == null || showtimeDatePicker.getValue() == null) throw new IllegalArgumentException("Hãy chọn phim, phòng và ngày chiếu");
            LocalDateTime start = LocalDateTime.of(showtimeDatePicker.getValue(), LocalTime.parse(showtimeTimeField.getText().trim()));
            Showtime showtime = new Showtime(UUID.randomUUID().toString(), movie, room, start,
                    start.plusMinutes(movie.getDuration() + 20L), ShowtimeStatus.SCHEDULED, new BigDecimal(showtimePriceField.getText().trim()));
            AppContext.get().managementService().createShowtime(showtime);
            DialogUtils.info("Đã tạo suất chiếu", showtime.getDetails()); loadData();
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tạo suất chiếu", exception); }
    }
}
