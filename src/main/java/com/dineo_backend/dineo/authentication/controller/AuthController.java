package com.dineo_backend.dineo.authentication.controller;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.service.AuthService;
import com.dineo_backend.dineo.config.AppConstants;
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
@RequestMapping("/api/auth")
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
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            String result = authService.registerUser(user);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstants.INTERNAL_ERROR);
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param loginRequest User login credentials
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        try {
            String result = authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AppConstants.INTERNAL_ERROR);
        }
    }
}
