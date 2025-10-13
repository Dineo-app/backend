package com.dineo_backend.dineo.authentication.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for user login request
 * Supports login with either email or phone number
 */
public class LoginRequest {

    @NotBlank(message = "Username (email or phone) is required")
    private String username; // Can be either email or phone

    @NotBlank(message = "Password is required")
    private String password;

    // Default constructor
    public LoginRequest() {}

    // Constructor with all fields
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @deprecated Use getUsername() instead
     * This method is kept for backward compatibility
     */
    @Deprecated
    public String getEmail() {
        return username;
    }

    /**
     * @deprecated Use setUsername() instead  
     * This method is kept for backward compatibility
     */
    @Deprecated
    public void setEmail(String email) {
        this.username = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Check if the username is an email format
     */
    public boolean isEmail() {
        return username != null && username.contains("@");
    }

    /**
     * Check if the username is a phone number format
     */
    public boolean isPhone() {
        return username != null && !username.contains("@") && username.matches("^[+]?[0-9\\s-]{8,15}$");
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}