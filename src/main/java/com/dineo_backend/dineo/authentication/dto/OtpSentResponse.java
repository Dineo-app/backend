package com.dineo_backend.dineo.authentication.dto;

/**
 * Response DTO for OTP sent confirmation
 * Includes information about OTP validity
 */
public class OtpSentResponse {
    
    private String message;
    private String phone;
    private int expiresInMinutes;
    private boolean canResend;
    
    // Constructors
    public OtpSentResponse() {}
    
    public OtpSentResponse(String message, String phone, int expiresInMinutes, boolean canResend) {
        this.message = message;
        this.phone = phone;
        this.expiresInMinutes = expiresInMinutes;
        this.canResend = canResend;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public int getExpiresInMinutes() {
        return expiresInMinutes;
    }
    
    public void setExpiresInMinutes(int expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }
    
    public boolean isCanResend() {
        return canResend;
    }
    
    public void setCanResend(boolean canResend) {
        this.canResend = canResend;
    }
}
