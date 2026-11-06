package com.cineticket.desktop.dao;

public final class DaoException extends RuntimeException {
    public DaoException(String message, Throwable cause) { super(message, cause); }
    public DaoException(String message) { super(message); }
}
