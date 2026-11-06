package com.cineticket.desktop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CouplePricing implements PricingStrategy {
    private static final BigDecimal MULTIPLIER = new BigDecimal("2.00");

    @Override
    public BigDecimal calculatePrice(ShowtimeSeat showtimeSeat) {
        return showtimeSeat.getBasePrice().multiply(MULTIPLIER).setScale(0, RoundingMode.HALF_UP);
    }
}
