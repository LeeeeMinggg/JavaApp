package com.cineticket.desktop.model;

import com.cineticket.desktop.observer.ObserverSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Payment implements NotificationSubject {
    private final String paymentId;
    private final BigDecimal amount;
    private final LocalDateTime paymentTime;
    private final String method;
    private PaymentStatus status;
    private final PaymentGateway gateway;
    private final ObserverSupport observers = new ObserverSupport();

    public Payment(String paymentId, BigDecimal amount, LocalDateTime paymentTime,
                   String method, PaymentStatus status, PaymentGateway gateway) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.amount = Objects.requireNonNull(amount);
        this.paymentTime = Objects.requireNonNull(paymentTime);
        this.method = Objects.requireNonNull(method);
        this.status = Objects.requireNonNull(status);
        this.gateway = Objects.requireNonNull(gateway);
    }

    public boolean processPayment() {
        boolean successful = gateway.processTransaction(this) && gateway.verifyTransaction(this);
        if (successful) markAsSuccessful();
        else {
            status = PaymentStatus.FAILED;
            notifyObservers("PAYMENT_FAILED:" + paymentId);
        }
        return successful;
    }

    public void markAsSuccessful() {
        status = PaymentStatus.SUCCESS;
        notifyObservers("PAYMENT_SUCCEEDED:" + paymentId);
    }

    public boolean refund() {
        if (status != PaymentStatus.SUCCESS) return false;
        status = PaymentStatus.REFUNDED;
        notifyObservers("PAYMENT_REFUNDED:" + paymentId);
        return true;
    }

    @Override public void subscribe(NotificationObserver observer) { observers.subscribe(observer); }
    @Override public void unsubscribe(NotificationObserver observer) { observers.unsubscribe(observer); }
    @Override public void notifyObservers(String event) { observers.notifyObservers(event); }
    public String getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public String getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public PaymentGateway getGateway() { return gateway; }
}
