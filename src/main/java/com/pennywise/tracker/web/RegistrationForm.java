package com.pennywise.tracker.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank(message = "Enter a username.")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters.")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "Use letters, numbers, dots, underscores, or hyphens only.")
    private String username;

    @NotBlank(message = "Enter a password.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
