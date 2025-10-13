package com.dineo_backend.dineo.authentication.controller;

import com.dineo_backend.dineo.authentication.dto.AuthData;
import com.dineo_backend.dineo.authentication.dto.LoginRequest;
import com.dineo_backend.dineo.authentication.dto.RefreshTokenRequest;
import com.dineo_backend.dineo.authentication.dto.UpdatePasswordRequest;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.service.AuthService;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.authentication.config.AppConstants;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for authentication operations.
 * Provides endpoints for user registration, login, and password management.
 * 
 * @author Dineo Backend Team
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;

    /**
     * Constructor for AuthController with dependency injection.
     * 
     * @param authService Service for authentication operations
     * @param jwtService Service for JWT token operations
     */
    @Autowired
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user with default CUSTOMER role.
     * 
     * @param user User registration data
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthData>> register(@Valid @RequestBody User user) {
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
            logger.error("Unexpected error during user registration", e);
            ApiResponse<AuthData> errorResponse = ApiResponse.internalError(AppConstants.INTERNAL_ERROR);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * Supports login with either email or phone number.
     * 
     * @param loginRequest User login credentials (email/phone and password)
     * @return ResponseEntity with JWT token or error message
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthData>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Use the username field which can be either email or phone
            ApiResponse<AuthData> result = authService.loginUser(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.status(result.getStatus()).body(result);
        } catch (IllegalArgumentException e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        } catch (RuntimeException e) {
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        } catch (Exception e) {
            logger.error("Unexpected error during user login", e);
            ApiResponse<AuthData> errorResponse = ApiResponse.internalError(AppConstants.INTERNAL_ERROR);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    /**
     * Updates user password after validating current password.
     * Extracts user ID from JWT token for authentication.
     * 
     * @param updatePasswordRequest Request containing old password, new password, and confirmation
     * @param request HTTP request to extract JWT token from Authorization header
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/update-password")
    public ResponseEntity<ApiResponse<String>> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest,
            HttpServletRequest request) {
        
        logger.info("Password update request received");
        
        try {
            // Extract JWT token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Missing or invalid Authorization header");
                ApiResponse<String> errorResponse = ApiResponse.error(401, AppConstants.JWT_TOKEN_MISSING);
                return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
            }

            String token = authHeader.substring(7);
            
            // Validate token and extract user ID
            try {
                if (!jwtService.isAccessToken(token)) {
                    logger.warn("Invalid token type provided");
                    ApiResponse<String> errorResponse = ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID);
                    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
                }
                
                String userIdString = jwtService.extractUsername(token); // This should extract the user ID now
                UUID userId = UUID.fromString(userIdString);
                
                // Validate token
                if (!jwtService.validateToken(token, userIdString)) {
                    logger.warn("Token validation failed");
                    ApiResponse<String> errorResponse = ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID);
                    return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
                }
                
                // Call service to update password
                ApiResponse<String> result = authService.updatePassword(userId, updatePasswordRequest);
                return ResponseEntity.status(result.getStatus()).body(result);
                
            } catch (Exception e) {
                logger.error("Error processing JWT token", e);
                ApiResponse<String> errorResponse = ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID);
                return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during password update", e);
            ApiResponse<String> errorResponse = ApiResponse.internalError(AppConstants.INTERNAL_ERROR);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * 
     * @param refreshTokenRequest Request containing the refresh token
     * @return ResponseEntity with new access token or error message
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthData>> refresh(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        
        logger.info("Token refresh request received");
        
        try {
            ApiResponse<AuthData> result = authService.refreshAccessToken(refreshTokenRequest.getRefreshToken());
            return ResponseEntity.status(result.getStatus()).body(result);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid refresh token: {}", e.getMessage());
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
            
        } catch (RuntimeException e) {
            logger.error("Runtime error during token refresh: {}", e.getMessage());
            ApiResponse<AuthData> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            ApiResponse<AuthData> errorResponse = ApiResponse.internalError(AppConstants.INTERNAL_ERROR);
            return ResponseEntity.status(errorResponse.getStatus()).body(errorResponse);
        }
    }
}
