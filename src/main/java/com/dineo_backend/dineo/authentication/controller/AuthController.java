package com.dineo_backend.dineo.authentication.controller;

import com.dineo_backend.dineo.authentication.dto.AuthData;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.service.AuthService;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user registration and login.
 * 
 * @author Dineo Backend Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor for AuthController with dependency injection.
     * 
     * @param authService Service for authentication operations
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user with default CUSTOMER role.
     * 
     * @param user User registration data
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthData>> register(@RequestBody User user) {
        try {
            ApiResponse<AuthData> result = authService.registerUser(user);
            return ResponseEntity.status(result.getStatus()).body(result);
        } catch (IllegalArgumentException e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        } catch (RuntimeException e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.internalError(AppConstants.INTERNAL_ERROR);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param loginRequest User login credentials
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthData>> login(@RequestBody User loginRequest) {
        try {
            ApiResponse<AuthData> result = authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.status(result.getStatus()).body(result);
        } catch (IllegalArgumentException e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        } catch (RuntimeException e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.internalError(AppConstants.INTERNAL_ERROR);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }
}
