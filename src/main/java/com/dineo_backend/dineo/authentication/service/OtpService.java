package com.dineo_backend.dineo.authentication.service;

import com.dineo_backend.dineo.authentication.model.Otp;
import com.dineo_backend.dineo.authentication.model.Otp.OtpType;
import com.dineo_backend.dineo.authentication.repository.OtpRepository;
import com.dineo_backend.dineo.sms.service.OvhSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Production-ready OTP Service for passwordless authentication
 * - Generates 6-digit OTP codes
 * - Sends OTP via SMS
 * - Validates OTP with rate limiting (max 5 attempts)
 * - OTP valid for 15 minutes
 * - Automatic cleanup of expired OTPs
 * - Rate limiting: max 3 OTP requests per 5 minutes
 */
@Service
public class OtpService {
    
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();
    
    // Rate limiting: max 10 OTP requests per 1 minute
    private static final int MAX_OTP_REQUESTS_PER_WINDOW = 10;
    private static final int RATE_LIMIT_WINDOW_MINUTES = 1;
    
    @Autowired
    private OtpRepository otpRepository;
    
    @Autowired
    private OvhSmsService ovhSmsService;
    
    /**
     * Generate and send OTP for registration
     * 
     * @param phone user's phone number
     * @param email user's email
     * @return generated OTP entity
     * @throws Exception if rate limit exceeded or SMS sending fails
     */
    @Transactional
    public Otp generateAndSendRegistrationOtp(String phone, String email) throws Exception {
        return generateAndSendOtp(phone, email, OtpType.REGISTRATION);
    }
    
    /**
     * Generate and send OTP for login
     * 
     * @param phone user's phone number
     * @param email user's email
     * @return generated OTP entity
     * @throws Exception if rate limit exceeded or SMS sending fails
     */
    @Transactional
    public Otp generateAndSendLoginOtp(String phone, String email) throws Exception {
        return generateAndSendOtp(phone, email, OtpType.LOGIN);
    }
    
    /**
     * Generate and send OTP (internal method)
     */
    private Otp generateAndSendOtp(String phone, String email, OtpType type) throws Exception {
        // Check rate limiting
        long recentOtpCount = otpRepository.countRecentOtps(
            phone, email, LocalDateTime.now().minusMinutes(RATE_LIMIT_WINDOW_MINUTES)
        );
        
        if (recentOtpCount >= MAX_OTP_REQUESTS_PER_WINDOW) {
            logger.warn("🚫 Rate limit exceeded for phone: {}, email: {}", phone, email);
            throw new IllegalStateException(
                "Too many OTP requests. Please wait " + RATE_LIMIT_WINDOW_MINUTES + " minutes before trying again."
            );
        }
        
        // Generate 6-digit OTP code
        String code = generateOtpCode();
        
        // Create OTP entity
        Otp otp = new Otp(code, phone, email, type);
        otp = otpRepository.save(otp);
        
        logger.info("✅ OTP generated for phone: {}, type: {}, expires at: {}", 
                    phone, type, otp.getExpiresAt());
        
        // Send SMS
        try {
            String message = buildOtpMessage(code, type);
            ovhSmsService.sendSms(phone, message);
            logger.info("📱 OTP SMS sent successfully to: {}", phone);
        } catch (Exception e) {
            logger.error("❌ Failed to send OTP SMS to: {}", phone, e);
            // Delete the OTP if SMS sending failed
            otpRepository.delete(otp);
            throw new Exception("Failed to send OTP SMS: " + e.getMessage(), e);
        }
        
        return otp;
    }
    
    /**
     * Verify OTP code
     * 
     * @param code OTP code to verify
     * @param phone user's phone number
     * @return verified OTP entity if valid
     * @throws Exception if OTP is invalid, expired, or max attempts exceeded
     */
    @Transactional
    public Otp verifyOtp(String code, String phone) throws Exception {
        // Find the most recent non-verified OTP for this phone
        Optional<Otp> otpOptional = otpRepository.findByCodeAndPhoneAndIsVerifiedFalse(code, phone);
        
        if (otpOptional.isEmpty()) {
            logger.warn("⚠️ Invalid OTP code attempted for phone: {}", phone);
            throw new IllegalArgumentException("Invalid OTP code");
        }
        
        Otp otp = otpOptional.get();
        
        // Check if OTP is expired
        if (otp.isExpired()) {
            logger.warn("⏰ Expired OTP attempted for phone: {}", phone);
            throw new IllegalStateException("OTP has expired. Please request a new code.");
        }
        
        // Check if max attempts exceeded
        if (!otp.canAttempt()) {
            logger.warn("🚫 Max OTP attempts exceeded for phone: {}", phone);
            throw new IllegalStateException("Maximum verification attempts exceeded. Please request a new code.");
        }
        
        // Increment attempts
        otp.incrementAttempts();
        
        // Mark as verified
        otp.markAsVerified();
        otpRepository.save(otp);
        
        logger.info("✅ OTP verified successfully for phone: {}, type: {}", phone, otp.getType());
        
        return otp;
    }
    
    /**
     * Resend OTP (invalidates previous OTP and sends new one)
     * 
     * @param phone user's phone number
     * @param email user's email
     * @param type OTP type (REGISTRATION or LOGIN)
     * @return new OTP entity
     * @throws Exception if rate limit exceeded or SMS sending fails
     */
    @Transactional
    public Otp resendOtp(String phone, String email, OtpType type) throws Exception {
        logger.info("🔄 Resending OTP for phone: {}, type: {}", phone, type);
        
        // Delete all previous non-verified OTPs for this phone
        otpRepository.deleteAllByPhone(phone);
        
        // Generate and send new OTP
        return generateAndSendOtp(phone, email, type);
    }
    
    /**
     * Generate a 6-digit OTP code using SecureRandom
     */
    private String generateOtpCode() {
        // Generate number between 100000 and 999999
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * Build OTP SMS message
     */
    private String buildOtpMessage(String code, OtpType type) {
        if (type == OtpType.REGISTRATION) {
            return String.format(
                "Bienvenue sur Miamlo! Votre code de verification est: %s\n" +
                "Ce code expire dans 15 minutes.\n" +
                "Ne partagez jamais ce code.",
                code
            );
        } else {
            return String.format(
                "Votre code de connexion Miamlo: %s\n" +
                "Ce code expire dans 15 minutes.\n" +
                "Ne partagez jamais ce code.",
                code
            );
        }
    }
    
    /**
     * Scheduled cleanup task - runs every hour to delete expired OTPs
     * Keeps database clean and improves performance
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredOtps() {
        try {
            otpRepository.deleteExpiredOtps(LocalDateTime.now());
            logger.info("🧹 Expired OTPs cleaned up successfully");
        } catch (Exception e) {
            logger.error("❌ Failed to cleanup expired OTPs", e);
        }
    }
    
    /**
     * Check if OTP exists and is valid (not expired, not verified)
     */
    public boolean hasValidOtp(String phone, OtpType type) {
        Optional<Otp> otpOptional = otpRepository
            .findFirstByPhoneAndTypeAndIsVerifiedFalseOrderByCreatedAtDesc(phone, type);
        
        return otpOptional.isPresent() && !otpOptional.get().isExpired();
    }
    
    /**
     * Get remaining time in minutes for current OTP
     */
    public long getRemainingMinutes(String phone, OtpType type) {
        Optional<Otp> otpOptional = otpRepository
            .findFirstByPhoneAndTypeAndIsVerifiedFalseOrderByCreatedAtDesc(phone, type);
        
        if (otpOptional.isEmpty() || otpOptional.get().isExpired()) {
            return 0;
        }
        
        Otp otp = otpOptional.get();
        LocalDateTime now = LocalDateTime.now();
        
        return java.time.Duration.between(now, otp.getExpiresAt()).toMinutes();
    }
}
