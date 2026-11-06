package com.cineticket.desktop.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordHasher {
    public String hash(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Máy không hỗ trợ SHA-256", exception);
        }
    }

    public boolean matches(String rawPassword, String expectedHash) {
        return MessageDigest.isEqual(hash(rawPassword).getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
