package com.dineo_backend.dineo.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response DTO for user information included in authentication responses
 */
public class UserInfo {
    
    private String id;
    
    @JsonProperty("first_name")
    private String firstName;
    
    @JsonProperty("last_name")
    private String lastName;
    
    private String email;
    
    private String phone;
    
    private String address;
    
    @JsonProperty("user_image")
    private String userImage;
    
    @JsonProperty("is_verified")
    private Boolean isVerified;
    
    private String role;
    
    // Default constructor
    public UserInfo() {
    }
    
    // Constructor with all fields
    public UserInfo(String id, String firstName, String lastName, String email, 
                   String phone, String address, String userImage, Boolean isVerified, String role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.userImage = userImage;
        this.isVerified = isVerified;
        this.role = role;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getUserImage() {
        return userImage;
    }
    
    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }
    
    public Boolean getIsVerified() {
        return isVerified;
    }
    
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}