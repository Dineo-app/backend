package com.dineo_backend.dineo.authentication.service;

import com.dineo_backend.dineo.authentication.dto.AuthResponse;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;

import java.util.UUID;

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
     * @return AuthResponse with JWT tokens and user information
     * @throws IllegalArgumentException if user data is invalid
     * @throws RuntimeException if user already exists or internal error occurs
     */
    AuthResponse registerUser(User user);

    /**
     * Authenticates a user with email and password credentials.
     * 
     * @param email The user's email address
     * @param password The user's plain text password
     * @return AuthResponse with JWT tokens and user information
     * @throws IllegalArgumentException if credentials are invalid
     * @throws RuntimeException if authentication fails or internal error occurs
     */
    AuthResponse loginUser(String email, String password);

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

    /**
     * Checks if a user has a specific role.
     * 
     * @param userId The user's UUID
     * @param role The role to check for
     * @return true if user has the role, false otherwise
     */
    boolean userHasRole(UUID userId, Role role);
}
