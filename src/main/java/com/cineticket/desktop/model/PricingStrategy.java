package com.cineticket.desktop.model;

import java.math.BigDecimal;

@FunctionalInterface
public interface PricingStrategy {
    BigDecimal calculatePrice(ShowtimeSeat showtimeSeat);
}
