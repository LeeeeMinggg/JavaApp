package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.service.BookingDetails;
import com.cineticket.desktop.service.TicketDetails;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Formatters;
import com.cineticket.desktop.ui.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.stream.Collectors;

public final class CheckoutController extends AuthenticatedController {
    @FXML private Label bookingCodeLabel;
    @FXML private Label movieLabel;
    @FXML private Label venueLabel;
    @FXML private Label timeLabel;
    @FXML private Label seatsLabel;
    @FXML private Label holdLabel;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodBox;
    @FXML private CheckBox simulateFailureCheckBox;

    @FXML private void initialize() {
        initializeHeader();
        BookingDetails booking = AppContext.get().viewState().selectedBooking();
        if (booking == null) { goHome(); return; }
        bookingCodeLabel.setText("#" + booking.bookingId().substring(0, 8).toUpperCase());
        movieLabel.setText(booking.movieTitle()); venueLabel.setText(booking.cinemaName() + " • " + booking.roomName());
        timeLabel.setText(Formatters.dateTime(booking.startTime()));
        seatsLabel.setText(booking.tickets().stream().map(TicketDetails::seatNumber).collect(Collectors.joining(", ")));
        holdLabel.setText("Giữ ghế đến " + Formatters.dateTime(booking.heldUntil()));
        totalLabel.setText(Formatters.money(booking.totalAmount()));
        paymentMethodBox.getItems().setAll("Thẻ ngân hàng mô phỏng", "Ví điện tử mô phỏng", "Tiền mặt tại quầy mô phỏng");
        paymentMethodBox.getSelectionModel().selectFirst();
    }

    @FXML private void pay() {
        BookingDetails booking = AppContext.get().viewState().selectedBooking();
        String method = simulateFailureCheckBox.isSelected() ? "MOCK_FAIL" : "MOCK_SUCCESS";
        try {
            boolean success = AppContext.get().bookingService().processPayment(booking.bookingId(), currentUser().getUserId(), method);
            if (!success) {
                DialogUtils.info("Mô phỏng thất bại", "Giao dịch đã được ghi nhận là FAILED. Bạn có thể bỏ chọn ô thất bại và thử lại.");
                return;
            }
            BookingDetails confirmed = AppContext.get().bookingService().findById(booking.bookingId());
            AppContext.get().viewState().selectedBooking(confirmed);
            Navigator.show("eticket.fxml", "Vé điện tử");
        } catch (RuntimeException exception) { DialogUtils.error("Thanh toán không thành công", exception); }
    }

    @FXML private void cancel() {
        BookingDetails booking = AppContext.get().viewState().selectedBooking();
        try {
            AppContext.get().bookingService().cancelBooking(booking.bookingId(), currentUser());
            AppContext.get().viewState().selectedBooking(null);
            goHome();
        } catch (RuntimeException exception) { DialogUtils.error("Không thể hủy booking", exception); }
    }
}
