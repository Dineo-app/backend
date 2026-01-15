package com.dineo_backend.dineo.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for passwordless login request - Step 1
 * User provides phone number to receive OTP
 */
public class LoginOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in international format (e.g., +33612345678)")
    private String phone;
    
    // Constructors
    public LoginOtpRequest() {}
    
    public LoginOtpRequest(String phone) {
        this.phone = phone;
    }
    
    // Getters and Setters
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
