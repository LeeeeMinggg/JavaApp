package com.cineticket.desktop.dao;

import com.cineticket.desktop.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
    Optional<User> findById(String userId);
    List<User> findAll();
    void createCustomer(User customer);
    void setActive(String userId, boolean active);
}
