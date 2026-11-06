package com.cineticket.desktop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class StandardPricing implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(ShowtimeSeat showtimeSeat) {
        return showtimeSeat.getBasePrice().setScale(0, RoundingMode.HALF_UP);
    }
}
