package com.cineticket.desktop.service;

import com.cineticket.desktop.config.ConnectionFactory;
import com.cineticket.desktop.dao.DaoException;
import com.cineticket.desktop.model.*;
import com.cineticket.desktop.observer.ConsoleNotificationObserver;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public final class BookingService {
    private final ConnectionFactory connectionFactory;
    private final Duration holdDuration;

    public BookingService(ConnectionFactory connectionFactory, Duration holdDuration) {
        this.connectionFactory = connectionFactory;
        this.holdDuration = holdDuration;
    }

    public BookingDetails createPendingBooking(Customer customer, String showtimeId, List<String> showtimeSeatIds) {
        List<String> uniqueSeatIds = showtimeSeatIds == null ? List.of() : showtimeSeatIds.stream().distinct().toList();
        if (uniqueSeatIds.isEmpty()) throw new IllegalArgumentException("Hãy chọn ít nhất một ghế");
        if (uniqueSeatIds.size() != showtimeSeatIds.size()) throw new IllegalArgumentException("Danh sách ghế bị trùng");

        String bookingId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime heldUntil = now.plus(holdDuration);
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                releaseExpiredSeats(connection, showtimeId);
                List<LockedSeat> seats = lockSeats(connection, showtimeId, uniqueSeatIds);
                if (seats.size() != uniqueSeatIds.size() || seats.stream().anyMatch(seat -> seat.status != SeatStatus.AVAILABLE)) {
                    throw new IllegalStateException("Một hoặc nhiều ghế vừa được người khác chọn. Vui lòng chọn lại.");
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO bookings(booking_id,customer_id,showtime_id,booking_time,total_amount,status) VALUES(?,?,?,?,0,'PENDING')")) {
                    statement.setString(1, bookingId); statement.setString(2, customer.getUserId());
                    statement.setString(3, showtimeId); statement.setTimestamp(4, Timestamp.valueOf(now)); statement.executeUpdate();
                }

                BigDecimal total = BigDecimal.ZERO;
                for (LockedSeat seat : seats) {
                    int updated;
                    try (PreparedStatement statement = connection.prepareStatement("""
                            UPDATE showtime_seats SET status='HELD',held_until=?,version=version+1
                            WHERE showtime_seat_id=? AND version=? AND status='AVAILABLE'
                            """)) {
                        statement.setTimestamp(1, Timestamp.valueOf(heldUntil)); statement.setString(2, seat.id);
                        statement.setInt(3, seat.version); updated = statement.executeUpdate();
                    }
                    if (updated != 1) throw new IllegalStateException("Ghế " + seat.number + " vừa được đặt bởi người khác");
                    String ticketId = UUID.randomUUID().toString();
                    String ticketCode = "CT-" + ticketId.substring(0, 8).toUpperCase();
                    try (PreparedStatement statement = connection.prepareStatement("""
                            INSERT INTO tickets(ticket_id,booking_id,showtime_seat_id,ticket_code,price,status)
                            VALUES(?,?,?,?,?,'PENDING')
                            """)) {
                        statement.setString(1, ticketId); statement.setString(2, bookingId); statement.setString(3, seat.id);
                        statement.setString(4, ticketCode); statement.setBigDecimal(5, seat.price); statement.executeUpdate();
                    }
                    total = total.add(seat.price);
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE bookings SET total_amount=? WHERE booking_id=?")) {
                    statement.setBigDecimal(1, total); statement.setString(2, bookingId); statement.executeUpdate();
                }
                connection.commit();
                return loadBooking(bookingId, connection);
            } catch (RuntimeException | SQLException exception) {
                rollback(connection);
                if (exception instanceof RuntimeException runtime) throw runtime;
                throw new DaoException("Không thể tạo booking", exception);
            }
        } catch (SQLException exception) {
            throw new DaoException("Không thể kết nối cơ sở dữ liệu", exception);
        }
    }

    public boolean processPayment(String bookingId, String customerId, String method) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BookingPaymentRow booking = lockBookingForPayment(connection, bookingId, customerId);
                if (booking.status != BookingStatus.PENDING) throw new IllegalStateException("Booking không còn chờ thanh toán");
                if (!allSeatsStillHeld(connection, bookingId)) {
                    expireBooking(connection, bookingId);
                    connection.commit();
                    throw new IllegalStateException("Thời gian giữ ghế đã hết. Vui lòng đặt lại.");
                }

                PaymentGateway gateway = new PaymentGateway("SIMULATED", "CineTicket Mock Gateway", "ACTIVE");
                Payment payment = new Payment(UUID.randomUUID().toString(), booking.total, LocalDateTime.now(),
                        method, PaymentStatus.PENDING, gateway);
                payment.subscribe(new ConsoleNotificationObserver());
                boolean success = payment.processPayment();
                insertPayment(connection, bookingId, payment);

                if (success) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE bookings SET status='CONFIRMED' WHERE booking_id=?")) {
                        statement.setString(1, bookingId); statement.executeUpdate();
                    }
                    try (PreparedStatement statement = connection.prepareStatement("""
                            UPDATE showtime_seats SET status='CONFIRMED',held_until=NULL,version=version+1
                            WHERE showtime_seat_id IN (SELECT showtime_seat_id FROM tickets WHERE booking_id=?) AND status='HELD'
                            """)) {
                        statement.setString(1, bookingId); statement.executeUpdate();
                    }
                    issueTicketsAndETickets(connection, bookingId);
                }
                connection.commit();
                return success;
            } catch (RuntimeException | SQLException exception) {
                rollback(connection);
                if (exception instanceof RuntimeException runtime) throw runtime;
                throw new DaoException("Không thể xử lý thanh toán", exception);
            }
        } catch (SQLException exception) { throw new DaoException("Không thể kết nối cơ sở dữ liệu", exception); }
    }

    public void cancelBooking(String bookingId, User requestingUser) {
        try (Connection connection = connectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String owner;
                BookingStatus status;
                try (PreparedStatement statement = connection.prepareStatement("SELECT customer_id,status FROM bookings WHERE booking_id=? FOR UPDATE")) {
                    statement.setString(1, bookingId);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) throw new IllegalArgumentException("Không tìm thấy booking");
                        owner = result.getString("customer_id"); status = BookingStatus.valueOf(result.getString("status"));
                    }
                }
                if (!owner.equals(requestingUser.getUserId()) && requestingUser.getRole() == UserRole.CUSTOMER) {
                    throw new SecurityException("Bạn không có quyền hủy booking này");
                }
                if (status == BookingStatus.CANCELLED || status == BookingStatus.EXPIRED) return;
                try (PreparedStatement statement = connection.prepareStatement("UPDATE payments SET status='REFUNDED' WHERE booking_id=? AND status='SUCCESS'")) {
                    statement.setString(1, bookingId); statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE tickets SET status='CANCELLED' WHERE booking_id=?")) {
                    statement.setString(1, bookingId); statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        UPDATE showtime_seats SET status='AVAILABLE',held_until=NULL,version=version+1
                        WHERE showtime_seat_id IN (SELECT showtime_seat_id FROM tickets WHERE booking_id=?)
                        """)) {
                    statement.setString(1, bookingId); statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE etickets SET status='CANCELLED' WHERE ticket_id IN (SELECT ticket_id FROM tickets WHERE booking_id=?)")) {
                    statement.setString(1, bookingId); statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("UPDATE bookings SET status='CANCELLED' WHERE booking_id=?")) {
                    statement.setString(1, bookingId); statement.executeUpdate();
                }
                connection.commit();
            } catch (RuntimeException | SQLException exception) {
                rollback(connection);
                if (exception instanceof RuntimeException runtime) throw runtime;
                throw new DaoException("Không thể hủy booking", exception);
            }
        } catch (SQLException exception) { throw new DaoException("Không thể kết nối cơ sở dữ liệu", exception); }
    }

    public List<BookingDetails> findHistory(String customerId) {
        List<String> ids = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT booking_id FROM bookings WHERE customer_id=? ORDER BY booking_time DESC")) {
            statement.setString(1, customerId);
            try (ResultSet result = statement.executeQuery()) { while (result.next()) ids.add(result.getString(1)); }
            List<BookingDetails> bookings = new ArrayList<>();
            for (String id : ids) bookings.add(loadBooking(id, connection));
            return bookings;
        } catch (SQLException exception) { throw new DaoException("Không thể tải lịch sử đặt vé", exception); }
    }

    public BookingDetails findById(String bookingId) {
        try (Connection connection = connectionFactory.getConnection()) { return loadBooking(bookingId, connection); }
        catch (SQLException exception) { throw new DaoException("Không thể tải booking", exception); }
    }

    private void releaseExpiredSeats(Connection connection, String showtimeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE showtime_seats SET status='AVAILABLE',held_until=NULL,version=version+1
                WHERE showtime_id=? AND status='HELD' AND held_until<CURRENT_TIMESTAMP
                """)) { statement.setString(1, showtimeId); statement.executeUpdate(); }
    }

    private List<LockedSeat> lockSeats(Connection connection, String showtimeId, List<String> ids) throws SQLException {
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT ss.showtime_seat_id,ss.status,ss.price,ss.version,s.seat_number FROM showtime_seats ss "
                + "JOIN seats s ON s.seat_id=ss.seat_id WHERE ss.showtime_id=? AND ss.showtime_seat_id IN (" + placeholders + ") FOR UPDATE";
        List<LockedSeat> seats = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, showtimeId);
            for (int index = 0; index < ids.size(); index++) statement.setString(index + 2, ids.get(index));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) seats.add(new LockedSeat(result.getString("showtime_seat_id"),
                        result.getString("seat_number"), SeatStatus.valueOf(result.getString("status")),
                        result.getBigDecimal("price"), result.getInt("version")));
            }
        }
        return seats;
    }

    private BookingPaymentRow lockBookingForPayment(Connection connection, String bookingId, String customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT customer_id,total_amount,status FROM bookings WHERE booking_id=? FOR UPDATE")) {
            statement.setString(1, bookingId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Không tìm thấy booking");
                if (!result.getString("customer_id").equals(customerId)) throw new SecurityException("Booking không thuộc tài khoản hiện tại");
                return new BookingPaymentRow(result.getBigDecimal("total_amount"), BookingStatus.valueOf(result.getString("status")));
            }
        }
    }

    private boolean allSeatsStillHeld(Connection connection, String bookingId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*) total, SUM(CASE WHEN ss.status='HELD' AND ss.held_until>CURRENT_TIMESTAMP THEN 1 ELSE 0 END) valid_count
                FROM tickets t JOIN showtime_seats ss ON ss.showtime_seat_id=t.showtime_seat_id WHERE t.booking_id=?
                """)) {
            statement.setString(1, bookingId);
            try (ResultSet result = statement.executeQuery()) { result.next(); return result.getInt("total") > 0 && result.getInt("total") == result.getInt("valid_count"); }
        }
    }

    private void insertPayment(Connection connection, String bookingId, Payment payment) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO payments(payment_id,booking_id,amount,payment_time,method,status,gateway_name) VALUES(?,?,?,?,?,?,?)
                """)) {
            statement.setString(1, payment.getPaymentId()); statement.setString(2, bookingId);
            statement.setBigDecimal(3, payment.getAmount()); statement.setTimestamp(4, Timestamp.valueOf(payment.getPaymentTime()));
            statement.setString(5, payment.getMethod()); statement.setString(6, payment.getStatus().name());
            statement.setString(7, payment.getGateway().getGatewayName()); statement.executeUpdate();
        }
    }

    private void issueTicketsAndETickets(Connection connection, String bookingId) throws SQLException {
        List<String> ticketIds = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT ticket_id FROM tickets WHERE booking_id=?")) {
            statement.setString(1, bookingId);
            try (ResultSet result = statement.executeQuery()) { while (result.next()) ticketIds.add(result.getString(1)); }
        }
        try (PreparedStatement statement = connection.prepareStatement("UPDATE tickets SET status='ISSUED' WHERE booking_id=?")) {
            statement.setString(1, bookingId); statement.executeUpdate();
        }
        for (String ticketId : ticketIds) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO etickets(eticket_id,ticket_id,qr_code,issue_date,status) VALUES(?,?,?,?, 'ACTIVE')
                    """)) {
                statement.setString(1, UUID.randomUUID().toString()); statement.setString(2, ticketId);
                statement.setString(3, "CINETICKET|" + bookingId + "|" + ticketId);
                statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now())); statement.executeUpdate();
            }
        }
    }

    private void expireBooking(Connection connection, String bookingId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE bookings SET status='EXPIRED' WHERE booking_id=?")) {
            statement.setString(1, bookingId); statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("UPDATE tickets SET status='CANCELLED' WHERE booking_id=?")) {
            statement.setString(1, bookingId); statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE showtime_seats SET status='AVAILABLE',held_until=NULL,version=version+1
                WHERE showtime_seat_id IN (SELECT showtime_seat_id FROM tickets WHERE booking_id=?) AND status='HELD'
                """)) { statement.setString(1, bookingId); statement.executeUpdate(); }
    }

    private BookingDetails loadBooking(String bookingId, Connection connection) throws SQLException {
        String sql = """
                SELECT b.*,m.title,c.name AS cinema_name,r.room_name,st.start_time,MAX(ss.held_until) held_until
                FROM bookings b JOIN showtimes st ON st.showtime_id=b.showtime_id JOIN movies m ON m.movie_id=st.movie_id
                JOIN rooms r ON r.room_id=st.room_id JOIN cinemas c ON c.cinema_id=r.cinema_id
                LEFT JOIN tickets t ON t.booking_id=b.booking_id LEFT JOIN showtime_seats ss ON ss.showtime_seat_id=t.showtime_seat_id
                WHERE b.booking_id=? GROUP BY b.booking_id,b.customer_id,b.showtime_id,b.booking_time,b.total_amount,b.status,
                m.title,c.name,r.room_name,st.start_time
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookingId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Không tìm thấy booking");
                Timestamp held = result.getTimestamp("held_until");
                return new BookingDetails(bookingId, result.getString("customer_id"), result.getString("title"),
                        result.getString("cinema_name"), result.getString("room_name"), result.getTimestamp("start_time").toLocalDateTime(),
                        result.getTimestamp("booking_time").toLocalDateTime(), result.getBigDecimal("total_amount"),
                        BookingStatus.valueOf(result.getString("status")), held == null ? null : held.toLocalDateTime(),
                        loadTickets(bookingId, connection));
            }
        }
    }

    private List<TicketDetails> loadTickets(String bookingId, Connection connection) throws SQLException {
        String sql = """
                SELECT t.*,s.seat_number,s.type,e.qr_code FROM tickets t
                JOIN showtime_seats ss ON ss.showtime_seat_id=t.showtime_seat_id JOIN seats s ON s.seat_id=ss.seat_id
                LEFT JOIN etickets e ON e.ticket_id=t.ticket_id WHERE t.booking_id=? ORDER BY s.seat_number
                """;
        List<TicketDetails> tickets = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, bookingId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) tickets.add(new TicketDetails(result.getString("ticket_id"), result.getString("ticket_code"),
                        result.getString("seat_number"), SeatType.valueOf(result.getString("type")), result.getBigDecimal("price"),
                        TicketStatus.valueOf(result.getString("status")), result.getString("qr_code")));
            }
        }
        return tickets;
    }

    private void rollback(Connection connection) { try { connection.rollback(); } catch (SQLException ignored) { } }
    private record LockedSeat(String id, String number, SeatStatus status, BigDecimal price, int version) { }
    private record BookingPaymentRow(BigDecimal total, BookingStatus status) { }
}
