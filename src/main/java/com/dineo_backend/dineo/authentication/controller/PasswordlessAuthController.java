package com.dineo_backend.dineo.authentication.controller;

import com.dineo_backend.dineo.authentication.dto.*;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.Otp;
import com.dineo_backend.dineo.authentication.model.Otp.OtpType;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.repository.UserRoleRepository;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.authentication.service.OtpService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Passwordless Authentication Controller
 * Handles OTP-based registration and login
 * 
 * Flow:
 * REGISTRATION:
 * 1. POST /register - Send user info, receive OTP via SMS
 * 2. POST /verify-registration - Verify OTP, create account, return JWT
 * 
 * LOGIN:
 * 1. POST /login - Send phone/email, receive OTP via SMS
 * 2. POST /verify-login - Verify OTP, return JWT
 * 
 * RESEND:
 * - POST /resend-otp - Resend OTP if expired or not received
 */
@RestController
@RequestMapping("/api/v1/auth/passwordless")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PasswordlessAuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordlessAuthController.class);
    
    @Autowired
    private OtpService otpService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Autowired
    private JwtService jwtService;
    
    /**
     * STEP 1: Registration - Send OTP
     * Validates user data and sends OTP to phone number
     * 
     * POST /api/v1/auth/passwordless/register
     * Body: {firstName, lastName, email, phone}
     * 
     * @param request Registration request with user details
     * @return OTP sent confirmation
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<OtpSentResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.info("📝 Registration request for phone: {}, email: {}", request.getPhone(), request.getEmail());
            
            // Check if email already exists
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                logger.warn("⚠️ Email already exists: {}", request.getEmail());
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Email is already registered"));
            }
            
            // Check if phone already exists
            if (userRepository.findByPhone(request.getPhone()).isPresent()) {
                logger.warn("⚠️ Phone already exists: {}", request.getPhone());
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Phone number is already registered"));
            }
            
            // Generate and send OTP
            Otp otp = otpService.generateAndSendRegistrationOtp(request.getPhone(), request.getEmail());
            
            OtpSentResponse response = new OtpSentResponse(
                "OTP sent to your phone number. Valid for 15 minutes.",
                maskPhone(request.getPhone()),
                15,
                true
            );
            
            logger.info("✅ OTP sent successfully for registration: {}", request.getPhone());
            
            return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully", response)
            );
            
        } catch (IllegalStateException e) {
            // Rate limit exceeded
            logger.warn("🚫 Rate limit exceeded: {}", e.getMessage());
            return ResponseEntity.status(429)
                .body(ApiResponse.error(429, e.getMessage()));
                
        } catch (Exception e) {
            logger.error("❌ Error sending registration OTP", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "Failed to send OTP: " + e.getMessage()));
        }
    }
    
    /**
     * STEP 2: Verify Registration OTP and Create Account
     * Creates user account after successful OTP verification
     * 
     * POST /api/v1/auth/verify-registration
     * Body: {phone, code, firstName, lastName, email}
     * 
     * @param request Combined verification and registration data
     * @return JWT tokens and user info
     */
    @PostMapping("/verify-registration")
    public ResponseEntity<ApiResponse<AuthData>> verifyRegistration(
            @Valid @RequestBody VerifyRegistrationRequest request) {
        try {
            logger.info("🔐 Verifying registration OTP for phone: {}", request.getPhone());
            
            // Verify OTP
            Otp otp = otpService.verifyOtp(request.getCode(), request.getPhone());
            
            if (otp.getType() != OtpType.REGISTRATION) {
                logger.warn("⚠️ Invalid OTP type for registration");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid OTP type"));
            }
            
            // Create user account
            User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone()
            );
            user.setVerified(true); // Mark as verified since OTP was successful
            user.setAddress(request.getAddress()); // Set address from registration
            
            user = userRepository.save(user);
            
            // Create CUSTOMER role for new user
            UserRole userRole = new UserRole(user.getId(), Role.CUSTOMER);
            userRoleRepository.save(userRole);
            logger.info("✅ Created CUSTOMER role for new user: {}", user.getEmail());
            
            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), null);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
            
            // Build response
            UserInfo userInfo = new UserInfo(
                user.getId().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                null, // userImage
                user.isVerified(),
                "CUSTOMER" // New users are always CUSTOMER
            );
            
            AuthData authData = new AuthData(accessToken, refreshToken, 1800000, user.getId().toString(), userInfo);
            
            logger.info("✅ Registration completed successfully for user: {}", user.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.success("Registration successful", authData)
            );
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("⚠️ OTP verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
                
        } catch (Exception e) {
            logger.error("❌ Error verifying registration OTP", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "Failed to complete registration: " + e.getMessage()));
        }
    }
    
    /**
     * STEP 1: Login - Send OTP
     * Sends OTP to user's phone number for authentication
     * 
     * POST /api/v1/auth/passwordless/login
     * Body: {phone}
     * 
     * @param request Login request with phone number
     * @return OTP sent confirmation
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OtpSentResponse>> login(@Valid @RequestBody LoginOtpRequest request) {
        try {
            logger.info("🔑 Login request for phone: {}", request.getPhone());
            
            // Find user by phone
            Optional<User> userOptional = userRepository.findByPhone(request.getPhone());
            
            if (userOptional.isEmpty()) {
                logger.warn("⚠️ User not found for login with phone: {}", request.getPhone());
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "User not found. Please register first."));
            }
            
            User user = userOptional.get();
            
            // Generate and send OTP
            Otp otp = otpService.generateAndSendLoginOtp(user.getPhone(), user.getEmail());
            
            OtpSentResponse response = new OtpSentResponse(
                "OTP sent to your phone number. Valid for 15 minutes.",
                maskPhone(user.getPhone()),
                15,
                true
            );
            
            logger.info("✅ Login OTP sent successfully to: {}", user.getPhone());
            
            return ResponseEntity.ok(
                ApiResponse.success("OTP sent successfully", response)
            );
            
        } catch (IllegalStateException e) {
            // Rate limit exceeded
            logger.warn("🚫 Rate limit exceeded: {}", e.getMessage());
            return ResponseEntity.status(429)
                .body(ApiResponse.error(429, e.getMessage()));
                
        } catch (Exception e) {
            logger.error("❌ Error sending login OTP", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "Failed to send OTP: " + e.getMessage()));
        }
    }
    
    /**
     * STEP 2: Verify Login OTP
     * Authenticates user and returns JWT tokens
     * 
     * POST /api/v1/auth/verify-login
     * Body: {phone, code}
     * 
     * @param request OTP verification request
     * @return JWT tokens and user info
     */
    @PostMapping("/verify-login")
    public ResponseEntity<ApiResponse<AuthData>> verifyLogin(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            logger.info("🔐 Verifying login OTP for phone: {}", request.getPhone());
            
            // Verify OTP
            Otp otp = otpService.verifyOtp(request.getCode(), request.getPhone());
            
            if (otp.getType() != OtpType.LOGIN) {
                logger.warn("⚠️ Invalid OTP type for login");
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid OTP type"));
            }
            
            // Find user
            Optional<User> userOptional = userRepository.findByPhone(request.getPhone());
            
            if (userOptional.isEmpty()) {
                logger.error("❌ User not found after OTP verification");
                return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, "User not found"));
            }
            
            User user = userOptional.get();
            
            // Mark user as verified if not already
            if (!user.isVerified()) {
                user.setVerified(true);
                userRepository.save(user);
            }
            
            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), null);
            String refreshToken = jwtService.generateRefreshToken(user.getId(), user.getEmail());
            
            // Fetch user's actual role from database
            String userRole = "CUSTOMER"; // Default fallback
            Optional<UserRole> roleOptional = userRoleRepository.findByUserId(user.getId());
            if (roleOptional.isPresent()) {
                userRole = roleOptional.get().getRole().name();
                logger.info("✅ User role found: {} for user: {}", userRole, user.getEmail());
            } else {
                logger.warn("⚠️ No role found for user: {}, defaulting to CUSTOMER", user.getEmail());
            }
            
            // Build response
            UserInfo userInfo = new UserInfo(
                user.getId().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                null, // userImage
                user.isVerified(),
                userRole // Use actual role from database
            );
            
            AuthData authData = new AuthData(accessToken, refreshToken, 1800000, user.getId().toString(), userInfo);
            
            logger.info("✅ Login successful for user: {}", user.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.success("Login successful", authData)
            );
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("⚠️ OTP verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
                
        } catch (Exception e) {
            logger.error("❌ Error verifying login OTP", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "Failed to verify OTP: " + e.getMessage()));
        }
    }
    
    /**
     * Resend OTP
     * Invalidates previous OTP and sends new one
     * 
     * POST /api/v1/auth/resend-otp
     * Body: {phone, email, type}
     * 
     * @param request Resend OTP request
     * @return New OTP sent confirmation
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<OtpSentResponse>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        try {
            logger.info("🔄 Resending OTP for phone: {}, type: {}", request.getPhone(), request.getType());
            
            OtpType type = OtpType.valueOf(request.getType());
            
            // Resend OTP
            Otp otp = otpService.resendOtp(request.getPhone(), request.getEmail(), type);
            
            OtpSentResponse response = new OtpSentResponse(
                "New OTP sent to your phone number. Valid for 15 minutes.",
                maskPhone(request.getPhone()),
                15,
                true
            );
            
            logger.info("✅ OTP resent successfully to: {}", request.getPhone());
            
            return ResponseEntity.ok(
                ApiResponse.success("OTP resent successfully", response)
            );
            
        } catch (IllegalStateException e) {
            logger.warn("🚫 Rate limit exceeded: {}", e.getMessage());
            return ResponseEntity.status(429)
                .body(ApiResponse.error(429, e.getMessage()));
                
        } catch (Exception e) {
            logger.error("❌ Error resending OTP", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "Failed to resend OTP: " + e.getMessage()));
        }
    }
    
    /**
     * Refresh access token using refresh token
     * 
     * POST /api/v1/auth/refresh
     * Body: {refreshToken}
     * 
     * @param request Refresh token request
     * @return New access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            if (!jwtService.isRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Invalid refresh token"));
            }
            
            String userId = jwtService.extractUsername(refreshToken);
            
            if (!jwtService.validateToken(refreshToken, userId)) {
                return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Refresh token expired or invalid"));
            }
            
            java.util.UUID userIdUUID = java.util.UUID.fromString(userId);
            String newAccessToken = jwtService.generateAccessToken(userIdUUID, userId, null);
            
            return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", newAccessToken)
            );
            
        } catch (Exception e) {
            logger.error("❌ Error refreshing token", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.error(500, "Failed to refresh token"));
        }
    }
    
    /**
     * Mask phone number for security (show last 4 digits)
     * Example: +33612345678 -> +336****5678
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }
        String prefix = phone.substring(0, 4);
        String suffix = phone.substring(phone.length() - 4);
        return prefix + "****" + suffix;
    }
}
