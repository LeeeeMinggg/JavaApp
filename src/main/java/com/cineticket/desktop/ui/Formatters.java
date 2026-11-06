package com.cineticket.desktop.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formatters {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH:mm • dd/MM/yyyy");
    private Formatters() { }
    public static String money(BigDecimal value) { return MONEY.format(value == null ? BigDecimal.ZERO : value); }
    public static String dateTime(LocalDateTime value) { return value == null ? "" : value.format(DATE_TIME); }
}
