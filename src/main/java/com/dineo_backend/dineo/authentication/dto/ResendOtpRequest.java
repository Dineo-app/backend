package com.dineo_backend.dineo.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for resending OTP
 */
public class ResendOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", 
             message = "Phone must be in international format (e.g., +33612345678)")
    private String phone;
    
    private String email;
    
    @NotBlank(message = "OTP type is required")
    @Pattern(regexp = "^(REGISTRATION|LOGIN)$", 
             message = "OTP type must be either REGISTRATION or LOGIN")
    private String type; // "REGISTRATION" or "LOGIN"
    
    // Constructors
    public ResendOtpRequest() {}
    
    public ResendOtpRequest(String phone, String email, String type) {
        this.phone = phone;
        this.email = email;
        this.type = type;
    }
    
    // Getters and Setters
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
