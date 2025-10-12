package com.dineo_backend.dineo.authentication.service.impl;

import com.dineo_backend.dineo.authentication.dto.AuthData;
import com.dineo_backend.dineo.authentication.dto.UpdatePasswordRequest;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.authentication.service.AuthService;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.authentication.config.AppConstants;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of {@link AuthService} providing authentication functionality.
 * This service handles user registration, login, and validation operations.
 * 
 * @author Dineo Backend Team
 * @version 1.0
 * @since 1.0
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Constructor for AuthServiceImpl with dependency injection.
     * 
     * @param userRepository Repository for user data operations
     * @param roleRepository Repository for role data operations
     * @param passwordEncoder Encoder for password hashing
     * @param jwtService Service for JWT token operations
     */
    @Autowired
    public AuthServiceImpl(UserRepository userRepository, 
                          RoleRepository roleRepository, 
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<AuthData> registerUser(User user) {
        try {
            // Validate user data
            if (!isValidUserData(user)) {
                throw new IllegalArgumentException(AppConstants.INVALID_USER_DATA);
            }

            // Check if user already exists
            if (userExists(user.getEmail())) {
                throw new RuntimeException(AppConstants.USER_ALREADY_EXISTS);
            }

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save user first
            User savedUser = userRepository.save(user);
            
            // Create UserRole with CUSTOMER role by default
            UserRole userRole = new UserRole(savedUser.getId(), Role.CUSTOMER);
            roleRepository.save(userRole);
            
            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(savedUser.getId(), savedUser.getEmail(), Role.CUSTOMER);
            String refreshToken = jwtService.generateRefreshToken(savedUser.getId(), savedUser.getEmail());
            
            // Create auth data
            AuthData authData = new AuthData(
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000, // Convert to seconds
                savedUser.getId().toString()
            );
            
            // Return success response with tokens
            return ApiResponse.created(AppConstants.USER_REGISTERED_SUCCESS, authData);
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(AppConstants.INTERNAL_ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<AuthData> loginUser(String email, String password) {
        try {
            // Validate input
            if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
                throw new IllegalArgumentException(AppConstants.INVALID_CREDENTIALS);
            }

            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new RuntimeException(AppConstants.INVALID_CREDENTIALS);
            }

            User user = userOpt.get();

            // Verify password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException(AppConstants.INVALID_CREDENTIALS);
            }

            // Get user role
            List<UserRole> userRoles = roleRepository.findByUserId(user.getId());
            Role userRole = userRoles.isEmpty() ? Role.CUSTOMER : userRoles.get(0).getRole();

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), userRole);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());

            // Create auth data
            AuthData authData = new AuthData(
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000, // Convert to seconds
                user.getId().toString()
            );

            // Return success response with tokens
            return ApiResponse.success(AppConstants.USER_LOGIN_SUCCESS, authData);
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(AppConstants.INTERNAL_ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userExists(String email) {
        try {
            if (!StringUtils.hasText(email)) {
                return false;
            }
            return userRepository.findByEmail(email).isPresent();
        } catch (Exception e) {
            throw new RuntimeException(AppConstants.INTERNAL_ERROR, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidUserData(User user) {
        if (user == null) {
            return false;
        }
        
        return StringUtils.hasText(user.getEmail()) &&
               StringUtils.hasText(user.getPassword()) &&
               StringUtils.hasText(user.getFirstName()) &&
               StringUtils.hasText(user.getLastName());
    }

    @Override
    public boolean userHasRole(java.util.UUID userId, Role role) {
        return roleRepository.existsByUserIdAndRole(userId, role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiResponse<String> updatePassword(UUID userId, UpdatePasswordRequest updatePasswordRequest) {
        logger.info("Password update request received for user ID: {}", userId);
        
        try {
            // Validate request data
            if (updatePasswordRequest.getOldPassword() == null || updatePasswordRequest.getOldPassword().trim().isEmpty()) {
                logger.warn("Old password is missing for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.OLD_PASSWORD_REQUIRED);
            }
            
            if (updatePasswordRequest.getNewPassword() == null || updatePasswordRequest.getNewPassword().trim().isEmpty()) {
                logger.warn("New password is missing for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.NEW_PASSWORD_REQUIRED);
            }
            
            if (updatePasswordRequest.getConfirmPassword() == null || updatePasswordRequest.getConfirmPassword().trim().isEmpty()) {
                logger.warn("Confirm password is missing for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.CONFIRM_PASSWORD_REQUIRED);
            }
            
            // Check if new password and confirm password match
            if (!updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getConfirmPassword())) {
                logger.warn("Password confirmation mismatch for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.PASSWORD_MISMATCH);
            }
            
            // Check if new password is different from old password
            if (updatePasswordRequest.getOldPassword().equals(updatePasswordRequest.getNewPassword())) {
                logger.warn("New password is same as current password for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.NEW_PASSWORD_SAME_AS_CURRENT);
            }
            
            // Find user by ID
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                logger.error("User not found with ID: {}", userId);
                return ApiResponse.error(404, AppConstants.USER_NOT_FOUND);
            }
            
            User user = optionalUser.get();
            
            // Verify current password
            if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getPassword())) {
                logger.warn("Current password verification failed for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.CURRENT_PASSWORD_INCORRECT);
            }
            
            // Validate new password strength (minimum 8 characters)
            if (updatePasswordRequest.getNewPassword().length() < 8) {
                logger.warn("New password too weak for user ID: {}", userId);
                return ApiResponse.error(400, AppConstants.PASSWORD_TOO_WEAK);
            }
            
            // Update password
            String encodedNewPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
            user.setPassword(encodedNewPassword);
            userRepository.save(user);
            
            logger.info("Password updated successfully for user ID: {}", userId);
            return ApiResponse.success(AppConstants.PASSWORD_UPDATED_SUCCESS, AppConstants.SUCCESS);
            
        } catch (Exception e) {
            logger.error("Error updating password for user ID: {}", userId, e);
            return ApiResponse.error(500, AppConstants.PASSWORD_UPDATE_FAILED);
        }
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * Validates the refresh token, extracts the user ID, and generates a new access token.
     * 
     * @param refreshToken The refresh token to validate
     * @return ApiResponse with new access token and user information
     */
    @Override
    public ApiResponse<AuthData> refreshAccessToken(String refreshToken) {
        logger.info("Token refresh request received");
        
        try {
            // Validate that token is provided
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                logger.warn("Refresh token is missing");
                return ApiResponse.error(400, AppConstants.JWT_TOKEN_MISSING);
            }
            
            // Check if token is a refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                logger.warn("Invalid token type - not a refresh token");
                return ApiResponse.error(400, AppConstants.JWT_TOKEN_INVALID);
            }
            
            // Check if token is expired
            if (jwtService.isTokenExpired(refreshToken)) {
                logger.warn("Refresh token is expired");
                return ApiResponse.error(401, AppConstants.JWT_TOKEN_EXPIRED);
            }
            
            // Extract user ID from refresh token
            UUID userId;
            try {
                userId = jwtService.extractUserId(refreshToken);
                logger.info("Extracted user ID from refresh token: {}", userId);
            } catch (Exception e) {
                logger.error("Failed to extract user ID from refresh token", e);
                return ApiResponse.error(400, AppConstants.JWT_TOKEN_INVALID);
            }
            
            // Validate token with user ID
            if (!jwtService.validateToken(refreshToken, userId)) {
                logger.warn("Refresh token validation failed for user ID: {}", userId);
                return ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID);
            }
            
            // Find user by ID
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                logger.error("User not found with ID: {}", userId);
                return ApiResponse.error(404, AppConstants.USER_NOT_FOUND);
            }
            
            User user = optionalUser.get();
            
            // Get user role
            List<UserRole> userRoles = roleRepository.findByUserId(user.getId());
            Role userRole = userRoles.isEmpty() ? Role.CUSTOMER : userRoles.get(0).getRole();
            
            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), userRole);
            
            // Create response with new access token and same refresh token
            AuthData authData = new AuthData(
                    newAccessToken,
                    refreshToken, // Keep the same refresh token
                    accessTokenExpiration / 1000, // Convert to seconds
                    user.getId().toString()
            );
            
            logger.info("New access token generated successfully for user ID: {}", userId);
            return ApiResponse.success("Token rafraîchi avec succès", authData);
            
        } catch (Exception e) {
            logger.error("Error refreshing access token", e);
            return ApiResponse.error(500, AppConstants.INTERNAL_ERROR);
        }
    }
}
