package com.dineo_backend.dineo.authentication.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for saving Expo push token request.
 * Contains the Expo push token to be saved for push notifications.
 */
public class PushTokenRequest {
    
    @NotBlank(message = "Le token de notification est obligatoire")
    private String pushToken;
    
    // Default constructor
    public PushTokenRequest() {
    }
    
    // Constructor with push token
    public PushTokenRequest(String pushToken) {
        this.pushToken = pushToken;
    }
    
    // Getter and Setter
    public String getPushToken() {
        return pushToken;
    }
    
    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}
