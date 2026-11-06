package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.model.BookingStatus;
import com.cineticket.desktop.service.BookingDetails;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Formatters;
import com.cineticket.desktop.ui.Navigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public final class BookingHistoryController extends AuthenticatedController {
    @FXML private TableView<BookingDetails> bookingTable;
    @FXML private Label emptyLabel;

    @FXML private void initialize() {
        initializeHeader(); configureTable(); loadHistory();
    }

    private void configureTable() {
        TableColumn<BookingDetails, String> movie = new TableColumn<>("Phim");
        movie.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().movieTitle())); movie.setPrefWidth(220);
        TableColumn<BookingDetails, String> time = new TableColumn<>("Suất chiếu");
        time.setCellValueFactory(row -> new SimpleStringProperty(Formatters.dateTime(row.getValue().startTime()))); time.setPrefWidth(165);
        TableColumn<BookingDetails, String> venue = new TableColumn<>("Rạp / phòng");
        venue.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().cinemaName() + " / " + row.getValue().roomName())); venue.setPrefWidth(230);
        TableColumn<BookingDetails, String> total = new TableColumn<>("Tổng tiền");
        total.setCellValueFactory(row -> new SimpleStringProperty(Formatters.money(row.getValue().totalAmount()))); total.setPrefWidth(130);
        TableColumn<BookingDetails, String> status = new TableColumn<>("Trạng thái");
        status.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().status().name())); status.setPrefWidth(120);
        bookingTable.getColumns().setAll(movie, time, venue, total, status);
    }

    private void loadHistory() {
        try {
            bookingTable.getItems().setAll(AppContext.get().bookingService().findHistory(currentUser().getUserId()));
            emptyLabel.setVisible(bookingTable.getItems().isEmpty());
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tải lịch sử", exception); }
    }

    @FXML private void openTicket() {
        BookingDetails booking = bookingTable.getSelectionModel().getSelectedItem();
        if (booking == null) { DialogUtils.info("Chọn booking", "Hãy chọn một booking trong bảng."); return; }
        AppContext.get().viewState().selectedBooking(booking);
        Navigator.show(booking.status() == BookingStatus.CONFIRMED ? "eticket.fxml" : "checkout.fxml",
                booking.status() == BookingStatus.CONFIRMED ? "Vé điện tử" : "Thanh toán");
    }

    @FXML private void cancelBooking() {
        BookingDetails booking = bookingTable.getSelectionModel().getSelectedItem();
        if (booking == null) { DialogUtils.info("Chọn booking", "Hãy chọn booking cần hủy."); return; }
        if (!DialogUtils.confirm("Hủy booking", "Hủy booking này và giải phóng các ghế?")) return;
        try { AppContext.get().bookingService().cancelBooking(booking.bookingId(), currentUser()); loadHistory(); }
        catch (RuntimeException exception) { DialogUtils.error("Không thể hủy booking", exception); }
    }
}
