package com.dineo_backend.dineo.authentication.service.impl;

import com.dineo_backend.dineo.authentication.dto.AuthData;
import com.dineo_backend.dineo.authentication.dto.UpdatePasswordRequest;
import com.dineo_backend.dineo.authentication.dto.UpdateProfileRequest;
import com.dineo_backend.dineo.authentication.dto.UserInfo;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserPushToken;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.repository.UserPushTokenRepository;
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
import org.springframework.transaction.annotation.Transactional;
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
    private final UserPushTokenRepository pushTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    /**
     * Constructor for AuthServiceImpl with dependency injection.
     * 
     * @param userRepository Repository for user data operations
     * @param roleRepository Repository for role data operations
     * @param pushTokenRepository Repository for push token operations
     * @param passwordEncoder Encoder for password hashing
     * @param jwtService Service for JWT token operations
     */
    @Autowired
    public AuthServiceImpl(UserRepository userRepository, 
                          RoleRepository roleRepository,
                          UserPushTokenRepository pushTokenRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.pushTokenRepository = pushTokenRepository;
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

            // Check if user already exists by email
            if (userExists(user.getEmail())) {
                throw new RuntimeException(AppConstants.USER_ALREADY_EXISTS);
            }

            // Check if phone number already exists (if phone is provided)
            if (StringUtils.hasText(user.getPhone()) && userRepository.existsByPhone(user.getPhone())) {
                throw new RuntimeException("Le numéro de téléphone est déjà enregistré.");
            }

            // Note: No password - using OTP authentication
            user.setVerified(false); // Will be verified via OTP

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
    public ApiResponse<AuthData> loginUser(String identifier, String password) {
        try {
            // Validate input
            if (!StringUtils.hasText(identifier) || !StringUtils.hasText(password)) {
                throw new IllegalArgumentException(AppConstants.INVALID_CREDENTIALS);
            }

            // Find user by email or phone
            Optional<User> userOpt = userRepository.findByEmailOrPhone(identifier);
            if (userOpt.isEmpty()) {
                throw new RuntimeException(AppConstants.INVALID_CREDENTIALS);
            }

            User user = userOpt.get();

            // Note: Password-based login deprecated - use OTP login instead
            throw new RuntimeException("Password authentication is no longer supported. Please use OTP login.");
            
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
            
            // Password update not supported in passwordless authentication
            logger.warn("Password update attempted but not supported in passwordless auth for user ID: {}", userId);
            return ApiResponse.error(400, "Password authentication is no longer supported. Authentication is now via OTP only.");
            
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
    
    /**
     * {@inheritDoc}
     * Saves push token to the user_push_tokens table for multi-device support.
     * If the token already exists for this user, it updates the last_used_at timestamp.
     */
    @Override
    @Transactional
    public ApiResponse<String> savePushToken(UUID userId, String pushToken) {
        logger.info("Saving push token for user ID: {}", userId);
        
        try {
            if (pushToken == null || pushToken.trim().isEmpty()) {
                logger.warn("Push token is missing");
                return ApiResponse.error(400, "Le token de notification est obligatoire");
            }
            
            // Check if user exists
            if (!userRepository.existsById(userId)) {
                logger.error("User not found with ID: {}", userId);
                return ApiResponse.error(404, AppConstants.USER_NOT_FOUND);
            }
            
            // Check if this token already exists for this user
            Optional<UserPushToken> existingToken = pushTokenRepository.findByUserIdAndPushToken(userId, pushToken);
            
            if (existingToken.isPresent()) {
                // Token already registered, just update last used time
                existingToken.get().updateLastUsed();
                pushTokenRepository.save(existingToken.get());
                logger.info("Push token already exists, updated last_used_at for user ID: {}", userId);
            } else {
                // Check if this token is registered to another user (device switched accounts)
                Optional<UserPushToken> tokenForOtherUser = pushTokenRepository.findByPushToken(pushToken);
                if (tokenForOtherUser.isPresent()) {
                    // Remove token from previous user
                    pushTokenRepository.delete(tokenForOtherUser.get());
                    logger.info("Removed push token from previous user");
                }
                
                // Create new token entry
                UserPushToken newToken = new UserPushToken(userId, pushToken);
                pushTokenRepository.save(newToken);
                logger.info("New push token saved for user ID: {}", userId);
            }
            
            return ApiResponse.success("Token de notification enregistré avec succès", "OK");
            
        } catch (Exception e) {
            logger.error("Error saving push token for user ID: {}", userId, e);
            return ApiResponse.error(500, AppConstants.INTERNAL_ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     * Clears the push token for the current device (not all devices).
     */
    @Override
    @Transactional
    public ApiResponse<String> clearPushToken(UUID userId, String pushToken) {
        logger.info("Clearing push token for user ID: {}", userId);
        
        try {
            if (pushToken != null && !pushToken.trim().isEmpty()) {
                // Clear specific token (logout from one device)
                pushTokenRepository.deleteByUserIdAndPushToken(userId, pushToken);
                logger.info("Push token cleared for user ID: {} on specific device", userId);
            } else {
                // If no token provided, log warning but don't fail
                logger.warn("No push token provided for clearing, user ID: {}", userId);
            }
            
            return ApiResponse.success("Token de notification supprimé avec succès", "OK");
            
        } catch (Exception e) {
            logger.error("Error clearing push token for user ID: {}", userId, e);
            return ApiResponse.error(500, AppConstants.INTERNAL_ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     * Clears all push tokens for a user (used when clearing all sessions).
     */
    @Override
    @Transactional
    public ApiResponse<String> clearPushToken(UUID userId) {
        logger.info("Clearing all push tokens for user ID: {}", userId);
        
        try {
            pushTokenRepository.deleteByUserId(userId);
            logger.info("All push tokens cleared for user ID: {}", userId);
            return ApiResponse.success("Tous les tokens de notification supprimés avec succès", "OK");
            
        } catch (Exception e) {
            logger.error("Error clearing all push tokens for user ID: {}", userId, e);
            return ApiResponse.error(500, AppConstants.INTERNAL_ERROR);
        }
    }
    
    /**
     * {@inheritDoc}
     * Updates user profile information.
     */
    @Override
    @Transactional
    public ApiResponse<UserInfo> updateProfile(UUID userId, UpdateProfileRequest request) {
        logger.info("Profile update request received for user ID: {}", userId);
        
        try {
            // Find user by ID
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                logger.error("User not found with ID: {}", userId);
                return ApiResponse.error(404, AppConstants.USER_NOT_FOUND);
            }
            
            User user = optionalUser.get();
            
            // Update firstName if provided
            if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
                user.setFirstName(request.getFirstName().trim());
            }
            
            // Update lastName if provided
            if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
                user.setLastName(request.getLastName().trim());
            }
            
            // Update email if provided and different
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                String newEmail = request.getEmail().trim().toLowerCase();
                if (!newEmail.equals(user.getEmail())) {
                    // Check if email is already taken by another user
                    Optional<User> existingUser = userRepository.findByEmail(newEmail);
                    if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                        logger.warn("Email already taken: {}", newEmail);
                        return ApiResponse.error(400, "Cet email est déjà utilisé par un autre compte");
                    }
                    user.setEmail(newEmail);
                }
            }
            
            // Note: Phone is immutable after registration and removed from UpdateProfileRequest
            
            // Update address if provided
            if (request.getAddress() != null) {
                user.setAddress(request.getAddress().trim());
            }
            
            // Save updated user
            User savedUser = userRepository.save(user);
            
            // Get user role
            List<UserRole> userRoles = roleRepository.findByUserId(savedUser.getId());
            Role userRole = userRoles.isEmpty() ? Role.CUSTOMER : userRoles.get(0).getRole();
            
            // Create user info response
            UserInfo userInfo = new UserInfo();
            userInfo.setId(savedUser.getId().toString());
            userInfo.setFirstName(savedUser.getFirstName());
            userInfo.setLastName(savedUser.getLastName());
            userInfo.setEmail(savedUser.getEmail());
            userInfo.setPhone(savedUser.getPhone());
            userInfo.setAddress(savedUser.getAddress());
            userInfo.setRole(userRole.name());
            
            logger.info("Profile updated successfully for user ID: {}", userId);
            return ApiResponse.success("Profil mis à jour avec succès", userInfo);
            
        } catch (Exception e) {
            logger.error("Error updating profile for user ID: {}", userId, e);
            return ApiResponse.error(500, AppConstants.INTERNAL_ERROR);
        }
    }
}
