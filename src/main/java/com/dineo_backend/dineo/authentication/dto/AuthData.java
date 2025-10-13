package com.dineo_backend.dineo.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for authentication data in API responses
 */
public class AuthData {
    
    @JsonProperty("access_token")
    private String accessToken;
    
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    @JsonProperty("expires_in")
    private long expiresIn;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user")
    private UserInfo user;
    
    // Default constructor
    public AuthData() {
    }
    
    // Constructor with all fields
    public AuthData(String accessToken, String refreshToken, long expiresIn, String userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
    }
    
    // Constructor with user information
    public AuthData(String accessToken, String refreshToken, long expiresIn, String userId, UserInfo user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.user = user;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }
}