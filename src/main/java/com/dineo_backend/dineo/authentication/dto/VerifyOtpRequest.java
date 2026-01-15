package com.dineo_backend.dineo.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for OTP verification - Step 2
 * Used for both registration and login verification
 */
public class VerifyOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", 
             message = "Phone must be in international format (e.g., +33612345678)")
    private String phone;
    
    @NotBlank(message = "OTP code is required")
    @Size(min = 6, max = 6, message = "OTP code must be exactly 6 digits")
    @Pattern(regexp = "^\\d{6}$", message = "OTP code must contain only digits")
    private String code;
    
    // Constructors
    public VerifyOtpRequest() {}
    
    public VerifyOtpRequest(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }
    
    // Getters and Setters
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
}
