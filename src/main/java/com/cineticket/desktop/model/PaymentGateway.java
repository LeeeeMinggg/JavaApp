package com.cineticket.desktop.model;

import java.util.Objects;

public final class PaymentGateway {
    private final String gatewayId;
    private final String gatewayName;
    private String status;

    public PaymentGateway(String gatewayId, String gatewayName, String status) {
        this.gatewayId = Objects.requireNonNull(gatewayId);
        this.gatewayName = Objects.requireNonNull(gatewayName);
        this.status = Objects.requireNonNull(status);
    }

    public boolean processTransaction(Payment payment) {
        return "ACTIVE".equalsIgnoreCase(status)
                && payment.getAmount().signum() > 0
                && !payment.getMethod().toUpperCase().contains("FAIL");
    }

    public boolean verifyTransaction(Payment payment) {
        return processTransaction(payment);
    }

    public String getGatewayId() { return gatewayId; }
    public String getGatewayName() { return gatewayName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = Objects.requireNonNull(status); }
}
