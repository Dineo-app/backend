package com.dineo_backend.dineo.config;

public final class AppConstants {
    // Success messages
    public static final String USER_REGISTERED_SUCCESS = "User registered successfully.";
    public static final String USER_LOGIN_SUCCESS = "Login successful.";

    // Error messages
    public static final String USER_ALREADY_EXISTS = "User already exists.";
    public static final String INVALID_CREDENTIALS = "Invalid email or password.";
    public static final String INVALID_USER_DATA = "User data is not valid.";
    public static final String ROLE_NOT_FOUND = "Default role not found.";
    public static final String INTERNAL_ERROR = "An internal error occurred.";

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
