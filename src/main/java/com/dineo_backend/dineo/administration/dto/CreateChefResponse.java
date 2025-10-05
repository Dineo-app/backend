package com.dineo_backend.dineo.administration.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for chef creation response
 * Contains created user info and chef description details
 */
public class CreateChefResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private Boolean isVerified;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Chef Description info
    private UUID chefDescriptionId;
    private String description;
    private List<String> categories;

    // Password info (for email notification)
    private String temporaryPassword;
    private Boolean emailSent;

    // Default constructor
    public CreateChefResponse() {}

    // Constructor with all fields
    public CreateChefResponse(UUID userId, String firstName, String lastName, String email,
                            String phone, String address, Boolean isVerified,
                            Boolean isActive, LocalDateTime createdAt, UUID chefDescriptionId,
                            String description, List<String> categories,
                            String temporaryPassword, Boolean emailSent) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isVerified = isVerified;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.chefDescriptionId = chefDescriptionId;
        this.description = description;
        this.categories = categories;
        this.temporaryPassword = temporaryPassword;
        this.emailSent = emailSent;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getChefDescriptionId() {
        return chefDescriptionId;
    }

    public void setChefDescriptionId(UUID chefDescriptionId) {
        this.chefDescriptionId = chefDescriptionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public void setTemporaryPassword(String temporaryPassword) {
        this.temporaryPassword = temporaryPassword;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    @Override
    public String toString() {
        return "CreateChefResponse{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", isVerified=" + isVerified +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", chefDescriptionId=" + chefDescriptionId +
                ", description='" + description + '\'' +
                ", categories=" + categories +
                ", temporaryPassword='[HIDDEN]'" +
                ", emailSent=" + emailSent +
                '}';
    }
}