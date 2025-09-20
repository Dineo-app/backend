package com.dineo_backend.dineo.authentication.service;

import com.dineo_backend.dineo.authentication.model.User;

/**
 * Authentication service interface providing user registration and login functionality.
 * This interface defines the contract for authentication operations in the application.
 * 
 * @author Dineo Backend Team
 * @version 1.0
 * @since 1.0
 */
public interface AuthService {

    /**
     * Registers a new user in the system with default CUSTOMER role.
     * 
     * @param user The user object containing registration information
     * @return A response message indicating the result of the registration
     * @throws IllegalArgumentException if user data is invalid
     * @throws RuntimeException if user already exists or internal error occurs
     */
    String registerUser(User user);

    /**
     * Authenticates a user with email and password credentials.
     * 
     * @param email The user's email address
     * @param password The user's plain text password
     * @return A JWT token if authentication is successful
     * @throws IllegalArgumentException if credentials are invalid
     * @throws RuntimeException if authentication fails or internal error occurs
     */
    String loginUser(String email, String password);

    /**
     * Checks if a user exists by email address.
     * 
     * @param email The email address to check
     * @return true if user exists, false otherwise
     */
    boolean userExists(String email);

    /**
     * Validates user input data for registration.
     * 
     * @param user The user object to validate
     * @return true if user data is valid, false otherwise
     */
    boolean isValidUserData(User user);
}
