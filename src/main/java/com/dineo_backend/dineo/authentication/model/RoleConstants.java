package com.dineo_backend.dineo.authentication.model;

/**
 * Constants for role names used throughout the application
 * This ensures consistency and prevents typos in role management
 */
public final class RoleConstants {
    
    public static final String CUSTOMER = "CUSTOMER";
    public static final String PROVIDER = "PROVIDER";
    public static final String ADMIN = "ADMIN";
    
    // Private constructor to prevent instantiation
    private RoleConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
