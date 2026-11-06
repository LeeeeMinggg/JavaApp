package com.cineticket.desktop.ui.controller;

import com.cineticket.desktop.AppContext;
import com.cineticket.desktop.dao.DashboardStats;
import com.cineticket.desktop.model.User;
import com.cineticket.desktop.service.RevenueRow;
import com.cineticket.desktop.ui.DialogUtils;
import com.cineticket.desktop.ui.Formatters;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class AdminDashboardController extends AuthenticatedController {
    @FXML private Label usersStatLabel;
    @FXML private Label moviesStatLabel;
    @FXML private Label showtimesStatLabel;
    @FXML private Label bookingsStatLabel;
    @FXML private Label ticketsStatLabel;
    @FXML private Label revenueStatLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableView<RevenueRow> revenueTable;

    @FXML private void initialize() { initializeHeader(); configureTables(); loadData(); }

    private void configureTables() {
        TableColumn<User, String> name = new TableColumn<>("Họ tên");
        name.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getFullName())); name.setPrefWidth(190);
        TableColumn<User, String> username = new TableColumn<>("Tên đăng nhập");
        username.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getUsername())); username.setPrefWidth(145);
        TableColumn<User, String> email = new TableColumn<>("Email");
        email.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getEmail())); email.setPrefWidth(240);
        TableColumn<User, String> role = new TableColumn<>("Vai trò");
        role.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().getRole().name())); role.setPrefWidth(100);
        TableColumn<User, String> active = new TableColumn<>("Trạng thái");
        active.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().isActive() ? "Đang hoạt động" : "Đã khóa")); active.setPrefWidth(130);
        userTable.getColumns().setAll(name, username, email, role, active);

        TableColumn<RevenueRow, String> date = new TableColumn<>("Ngày");
        date.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().date().toString())); date.setPrefWidth(160);
        TableColumn<RevenueRow, String> bookings = new TableColumn<>("Booking");
        bookings.setCellValueFactory(row -> new SimpleStringProperty(String.valueOf(row.getValue().bookings()))); bookings.setPrefWidth(130);
        TableColumn<RevenueRow, String> tickets = new TableColumn<>("Vé / giao dịch");
        tickets.setCellValueFactory(row -> new SimpleStringProperty(String.valueOf(row.getValue().tickets()))); tickets.setPrefWidth(160);
        TableColumn<RevenueRow, String> revenue = new TableColumn<>("Doanh thu");
        revenue.setCellValueFactory(row -> new SimpleStringProperty(Formatters.money(row.getValue().revenue()))); revenue.setPrefWidth(190);
        revenueTable.getColumns().setAll(date, bookings, tickets, revenue);
    }

    private void loadData() {
        try {
            DashboardStats stats = AppContext.get().adminService().dashboardStats();
            usersStatLabel.setText(String.valueOf(stats.totalUsers())); moviesStatLabel.setText(String.valueOf(stats.activeMovies()));
            showtimesStatLabel.setText(String.valueOf(stats.futureShowtimes())); bookingsStatLabel.setText(String.valueOf(stats.confirmedBookings()));
            ticketsStatLabel.setText(String.valueOf(stats.issuedTickets())); revenueStatLabel.setText(Formatters.money(stats.totalRevenue()));
            userTable.getItems().setAll(AppContext.get().adminService().users());
            revenueTable.getItems().setAll(AppContext.get().adminService().revenueByDay());
        } catch (RuntimeException exception) { DialogUtils.error("Không thể tải dashboard", exception); }
    }

    @FXML private void toggleUserActive() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { DialogUtils.info("Chọn tài khoản", "Hãy chọn tài khoản cần khóa hoặc mở khóa."); return; }
        if (selected.getUserId().equals(currentUser().getUserId())) { DialogUtils.info("Không thể thực hiện", "Bạn không thể khóa chính tài khoản đang đăng nhập."); return; }
        try { AppContext.get().adminService().setUserActive(selected.getUserId(), !selected.isActive()); loadData(); }
        catch (RuntimeException exception) { DialogUtils.error("Không thể cập nhật tài khoản", exception); }
    }

    @FXML private void refresh() { loadData(); }
}
