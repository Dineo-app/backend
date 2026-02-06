package com.dineo_backend.dineo.administration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for chef list item - contains basic chef information for admin list view
 */
public class ChefListItemResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private boolean isVerified;
    private int totalPlates;
    private int totalOrders;
    private LocalDateTime createdAt;

    public ChefListItemResponse() {}

    public ChefListItemResponse(UUID id, String firstName, String lastName, String email, String phone, 
                               String address, boolean isVerified, int totalPlates, int totalOrders, 
                               LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isVerified = isVerified;
        this.totalPlates = totalPlates;
        this.totalOrders = totalOrders;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public int getTotalPlates() { return totalPlates; }
    public void setTotalPlates(int totalPlates) { this.totalPlates = totalPlates; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
