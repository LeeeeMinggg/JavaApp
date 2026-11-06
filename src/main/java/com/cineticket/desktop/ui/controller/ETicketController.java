package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.service.BookingDetails;
import com.cineticket.desktop.service.TicketDetails;
import com.cineticket.desktop.ui.Formatters;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public final class ETicketController extends AuthenticatedController {
    @FXML private Label statusLabel;
    @FXML private Label movieLabel;
    @FXML private Label venueLabel;
    @FXML private Label timeLabel;
    @FXML private Label bookingCodeLabel;
    @FXML private ListView<String> ticketList;
    @FXML private ImageView qrImage;

    @FXML private void initialize() {
        initializeHeader();
        BookingDetails booking = AppContext.get().viewState().selectedBooking();
        if (booking == null) { goHistory(); return; }
        statusLabel.setText("✓ " + booking.status()); movieLabel.setText(booking.movieTitle());
        venueLabel.setText(booking.cinemaName() + " • " + booking.roomName());
        timeLabel.setText(Formatters.dateTime(booking.startTime()));
        bookingCodeLabel.setText("Booking #" + booking.bookingId().substring(0, 8).toUpperCase());
        booking.tickets().forEach(ticket -> ticketList.getItems().add(
                ticket.ticketCode() + "  •  Ghế " + ticket.seatNumber() + "  •  " + Formatters.money(ticket.price())));
        booking.tickets().stream().map(TicketDetails::qrCode).filter(value -> value != null && !value.isBlank()).findFirst()
                .ifPresent(value -> qrImage.setImage(createQr(value)));
    }

    private WritableImage createQr(String value) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, 240, 240);
            WritableImage image = new WritableImage(matrix.getWidth(), matrix.getHeight());
            for (int y = 0; y < matrix.getHeight(); y++) {
                for (int x = 0; x < matrix.getWidth(); x++) {
                    image.getPixelWriter().setColor(x, y, matrix.get(x, y) ? Color.web("#0b132b") : Color.WHITE);
                }
            }
            return image;
        } catch (Exception exception) { throw new IllegalStateException("Không thể tạo mã QR", exception); }
    }
}
