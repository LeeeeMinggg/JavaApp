package com.cineticket.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class RevenueReport {
    private final String reportId;
    private BigDecimal totalRevenue;
    private int totalTickets;
    private final LocalDate reportDate;
    private List<Payment> payments = List.of();
    private List<Ticket> tickets = List.of();

    public RevenueReport(String reportId, LocalDate reportDate) {
        this.reportId = Objects.requireNonNull(reportId);
        this.reportDate = Objects.requireNonNull(reportDate);
        this.totalRevenue = BigDecimal.ZERO;
    }

    public void useData(List<Payment> payments, List<Ticket> tickets) {
        this.payments = List.copyOf(payments);
        this.tickets = List.copyOf(tickets);
    }

    public BigDecimal calculateRevenue() {
        totalRevenue = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalTickets = (int) tickets.stream().filter(Ticket::validate).count();
        return totalRevenue;
    }

    public RevenueReport generateReport() { calculateRevenue(); return this; }
    public String getReportId() { return reportId; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public int getTotalTickets() { return totalTickets; }
    public LocalDate getReportDate() { return reportDate; }
}
