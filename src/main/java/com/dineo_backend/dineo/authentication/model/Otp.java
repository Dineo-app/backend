package com.dineo_backend.dineo.authentication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OTP (One-Time Password) Entity for passwordless authentication
 * Valid for 15 minutes from creation
 */
@Entity
@Table(name = "otps", indexes = {
    @Index(name = "idx_otp_code", columnList = "code"),
    @Index(name = "idx_otp_phone", columnList = "phone"),
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_expires", columnList = "expires_at")
})
public class Otp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @NotBlank
    @Size(min = 6, max = 6)
    @Column(nullable = false, length = 6)
    private String code;
    
    @Size(max = 15)
    @Column(length = 15)
    private String phone;
    
    @Size(max = 100)
    @Column(length = 100)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpType type; // REGISTRATION or LOGIN
    
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;
    
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    public Otp() {
        this.createdAt = LocalDateTime.now();
        // OTP valid for 15 minutes
        this.expiresAt = this.createdAt.plusMinutes(15);
    }
    
    public Otp(String code, String phone, String email, OtpType type) {
        this();
        this.code = code;
        this.phone = phone;
        this.email = email;
        this.type = type;
    }
    
    // Check if OTP is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    // Check if OTP can still be attempted (max 5 attempts)
    public boolean canAttempt() {
        return this.attempts < 5;
    }
    
    // Increment attempt counter
    public void incrementAttempts() {
        this.attempts++;
    }
    
    // Mark as verified
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public OtpType getType() {
        return type;
    }
    
    public void setType(OtpType type) {
        this.type = type;
    }
    
    public boolean isVerified() {
        return isVerified;
    }
    
    public void setVerified(boolean verified) {
        isVerified = verified;
    }
    
    public int getAttempts() {
        return attempts;
    }
    
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
    
    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    /**
     * OTP Type Enum
     */
    public enum OtpType {
        REGISTRATION,  // For new user registration
        LOGIN          // For existing user login
    }
}
