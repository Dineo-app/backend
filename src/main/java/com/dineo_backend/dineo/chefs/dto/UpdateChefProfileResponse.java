package com.dineo_backend.dineo.chefs.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for chef profile update response
 * Contains updated user and chef description information
 */
public class UpdateChefProfileResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime userUpdatedAt;

    private UUID chefDescriptionId;
    private String description;
    private List<String> categories;
    private LocalDateTime chefDescriptionUpdatedAt;

    // Default constructor
    public UpdateChefProfileResponse() {}

    // Constructor with all fields
    public UpdateChefProfileResponse(UUID userId, String firstName, String lastName, String email,
                                   String phone, String address, LocalDateTime userUpdatedAt,
                                   UUID chefDescriptionId, String description, List<String> categories,
                                   LocalDateTime chefDescriptionUpdatedAt) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.userUpdatedAt = userUpdatedAt;
        this.chefDescriptionId = chefDescriptionId;
        this.description = description;
        this.categories = categories;
        this.chefDescriptionUpdatedAt = chefDescriptionUpdatedAt;
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

    public LocalDateTime getUserUpdatedAt() {
        return userUpdatedAt;
    }

    public void setUserUpdatedAt(LocalDateTime userUpdatedAt) {
        this.userUpdatedAt = userUpdatedAt;
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

    public LocalDateTime getChefDescriptionUpdatedAt() {
        return chefDescriptionUpdatedAt;
    }

    public void setChefDescriptionUpdatedAt(LocalDateTime chefDescriptionUpdatedAt) {
        this.chefDescriptionUpdatedAt = chefDescriptionUpdatedAt;
    }

    @Override
    public String toString() {
        return "UpdateChefProfileResponse{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", userUpdatedAt=" + userUpdatedAt +
                ", chefDescriptionId=" + chefDescriptionId +
                ", description='" + description + '\'' +
                ", categories=" + categories +
                ", chefDescriptionUpdatedAt=" + chefDescriptionUpdatedAt +
                '}';
    }
}