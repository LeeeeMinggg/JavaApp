package com.cineticket.desktop.model;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ETicket {
    private final String eTicketId;
    private final Ticket ticket;
    private final String qrCode;
    private final LocalDateTime issueDate;
    private String status;

    public ETicket(String eTicketId, Ticket ticket, String qrCode, LocalDateTime issueDate, String status) {
        this.eTicketId = Objects.requireNonNull(eTicketId);
        this.ticket = Objects.requireNonNull(ticket);
        this.qrCode = Objects.requireNonNull(qrCode);
        this.issueDate = Objects.requireNonNull(issueDate);
        this.status = Objects.requireNonNull(status);
    }

    public String generateQRCode() { return qrCode; }
    public String displayTicket() {
        return "E-TICKET " + ticket.getTicketCode() + "\n" + ticket.getShowtimeSeat().getShowtime().getDetails()
                + "\nGhế " + ticket.getShowtimeSeat().getSeat().getSeatNumber();
    }
    public void cancel() { status = "CANCELLED"; }
    public String getETicketId() { return eTicketId; }
    public Ticket getTicket() { return ticket; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public String getStatus() { return status; }
}
