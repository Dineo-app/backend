package com.dineo_backend.dineo.authentication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to store multiple push tokens per user for multi-device support.
 * Each device the user logs into will have its own push token entry.
 */
@Entity
@Table(name = "user_push_tokens", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "push_token"}))
public class UserPushToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "push_token", nullable = false)
    private String pushToken;
    
    @Column(name = "device_info")
    private String deviceInfo;  // Optional: store device name/model
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    // Default constructor
    public UserPushToken() {
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
    }
    
    // Constructor with required fields
    public UserPushToken(UUID userId, String pushToken) {
        this();
        this.userId = userId;
        this.pushToken = pushToken;
    }
    
    // Constructor with device info
    public UserPushToken(UUID userId, String pushToken, String deviceInfo) {
        this(userId, pushToken);
        this.deviceInfo = deviceInfo;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public UUID getUserId() {
        return userId;
    }
    
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    
    public String getPushToken() {
        return pushToken;
    }
    
    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
    
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
