package com.cineticket.desktop.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Ticket {
    private final String ticketId;
    private final String ticketCode;
    private final ShowtimeSeat showtimeSeat;
    private final BigDecimal price;
    private TicketStatus status;
    private ETicket eTicket;

    public Ticket(String ticketId, String ticketCode, ShowtimeSeat showtimeSeat,
                  BigDecimal price, TicketStatus status) {
        this.ticketId = Objects.requireNonNull(ticketId);
        this.ticketCode = Objects.requireNonNull(ticketCode);
        this.showtimeSeat = Objects.requireNonNull(showtimeSeat);
        this.price = Objects.requireNonNull(price);
        this.status = Objects.requireNonNull(status);
    }

    public void issue() {
        if (status != TicketStatus.PENDING) throw new IllegalStateException("Vé không ở trạng thái chờ");
        status = TicketStatus.ISSUED;
    }
    public boolean validate() { return status == TicketStatus.ISSUED; }
    public void cancel() { status = TicketStatus.CANCELLED; if (eTicket != null) eTicket.cancel(); }
    public String getTicketId() { return ticketId; }
    public String getTicketCode() { return ticketCode; }
    public ShowtimeSeat getShowtimeSeat() { return showtimeSeat; }
    public BigDecimal getPrice() { return price; }
    public TicketStatus getStatus() { return status; }
    public ETicket getETicket() { return eTicket; }
    public void setETicket(ETicket eTicket) { this.eTicket = eTicket; }
}
