package com.dineo_backend.dineo.authentication.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for refresh token request
 * Contains the refresh token to be validated and used to generate a new access token
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "Le token de rafraîchissement est obligatoire")
    private String refreshToken;
    
    // Default constructor
    public RefreshTokenRequest() {
    }
    
    // Constructor with refresh token
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getter and Setter
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "RefreshTokenRequest{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}
