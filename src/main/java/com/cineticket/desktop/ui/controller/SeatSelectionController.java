package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.dao.SeatView;
import com.cineticket.desktop.model.Customer;
import com.cineticket.desktop.model.Showtime;
import com.cineticket.desktop.service.BookingDetails;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Formatters;
import com.cineticket.desktop.ui.MovieArtwork;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public final class SeatSelectionController extends AuthenticatedController {
    @FXML private Label movieLabel;
    @FXML private Label showtimeLabel;
    @FXML private GridPane seatGrid;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label totalLabel;
    @FXML private Button continueButton;
    @FXML private ImageView summaryPoster;
    private final Map<String, SeatView> seatsById = new LinkedHashMap<>();
    private final Set<String> selectedIds = new LinkedHashSet<>();

    @FXML private void initialize() {
        initializeHeader();
        Showtime showtime = AppContext.get().viewState().selectedShowtime();
        if (showtime == null) { goHome(); return; }
        movieLabel.setText(showtime.getMovie().getTitle());
        summaryPoster.setImage(MovieArtwork.poster(showtime.getMovie()));
        showtimeLabel.setText(Formatters.dateTime(showtime.getStartTime()) + " • " + showtime.getRoom());
        loadSeats(showtime);
    }

    private void loadSeats(Showtime showtime) {
        try {
            List<SeatView> seats = AppContext.get().showtimeDao().findSeats(showtime.getShowtimeId());
            seatGrid.getChildren().clear(); seatsById.clear(); selectedIds.clear();
            Set<Integer> rows = new TreeSet<>();
            for (SeatView seat : seats) {
                seatsById.put(seat.showtimeSeatId(), seat);
                Button button = new Button(seat.seatNumber());
                button.getStyleClass().addAll("seat", "seat-" + seat.seatType().name().toLowerCase());
                boolean selectable = seat.selectable(LocalDateTime.now());
                if (!selectable) button.getStyleClass().add("seat-unavailable");
                button.setDisable(!selectable);
                button.setOnAction(event -> toggleSeat(seat, button));
                int row = Character.toUpperCase(seat.seatNumber().charAt(0)) - 'A';
                int column = Integer.parseInt(seat.seatNumber().substring(1)) - 1;
                rows.add(row);
                seatGrid.add(button, column + 1, row);
            }
            int seatColumns = seats.stream().mapToInt(seat -> Integer.parseInt(seat.seatNumber().substring(1))).max().orElse(8);
            for (int row : rows) {
                String rowName = String.valueOf((char) ('A' + row));
                Label left = new Label(rowName); left.getStyleClass().add("seat-row-label");
                Label right = new Label(rowName); right.getStyleClass().add("seat-row-label");
                seatGrid.add(left, 0, row); seatGrid.add(right, seatColumns + 1, row);
            }
            refreshSummary();
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tải sơ đồ ghế", exception); }
    }

    private void toggleSeat(SeatView seat, Button button) {
        if (selectedIds.remove(seat.showtimeSeatId())) button.getStyleClass().remove("seat-selected");
        else { selectedIds.add(seat.showtimeSeatId()); button.getStyleClass().add("seat-selected"); }
        refreshSummary();
    }

    private void refreshSummary() {
        List<String> numbers = selectedIds.stream().map(id -> seatsById.get(id).seatNumber()).toList();
        BigDecimal total = selectedIds.stream().map(id -> seatsById.get(id).price()).reduce(BigDecimal.ZERO, BigDecimal::add);
        selectedSeatsLabel.setText(numbers.isEmpty() ? "Chưa chọn ghế" : String.join(", ", numbers));
        totalLabel.setText(Formatters.money(total)); continueButton.setDisable(selectedIds.isEmpty());
    }

    @FXML private void continueToCheckout() {
        try {
            Showtime showtime = AppContext.get().viewState().selectedShowtime();
            Customer customer = (Customer) currentUser();
            BookingDetails booking = AppContext.get().bookingService().createPendingBooking(customer, showtime.getShowtimeId(), new ArrayList<>(selectedIds));
            AppContext.get().viewState().selectedBooking(booking);
            Navigator.show("checkout.fxml", "Thanh toán mô phỏng");
        } catch (RuntimeException exception) {
            DialogUtils.error("Không thể giữ ghế", exception);
            loadSeats(AppContext.get().viewState().selectedShowtime());
        }
    }

    @FXML private void back() { Navigator.show("movie-details.fxml", movieLabel.getText()); }
}
