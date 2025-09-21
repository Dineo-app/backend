package com.dineo_backend.dineo.authentication.service.impl;

import com.dineo_backend.dineo.authentication.dto.AuthResponse;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.authentication.service.AuthService;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.config.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

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
    public AuthResponse registerUser(User user) {
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
            
            // Return success response with tokens
            return new AuthResponse(
                AppConstants.USER_REGISTERED_SUCCESS,
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000 // Convert to seconds
            );
            
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
    public AuthResponse loginUser(String email, String password) {
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

            // Return success response with tokens
            return new AuthResponse(
                AppConstants.USER_LOGIN_SUCCESS,
                accessToken,
                refreshToken,
                accessTokenExpiration / 1000 // Convert to seconds
            );
            
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
}
