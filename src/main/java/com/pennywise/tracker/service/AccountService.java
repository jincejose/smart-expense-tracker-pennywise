package com.pennywise.tracker.service;

import com.pennywise.tracker.model.AppUser;
import com.pennywise.tracker.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createAccount(String username, String password) {
        String normalizedUsername = username.trim().toLowerCase(java.util.Locale.ROOT);
        if (users.findByUsername(normalizedUsername).isPresent()) {
            throw new AccountAlreadyExistsException();
        }
        try {
            users.saveAndFlush(new AppUser(normalizedUsername, passwordEncoder.encode(password)));
        } catch (DataIntegrityViolationException ex) {
            throw new AccountAlreadyExistsException();
        }
    }

    public static class AccountAlreadyExistsException extends RuntimeException { }
}
