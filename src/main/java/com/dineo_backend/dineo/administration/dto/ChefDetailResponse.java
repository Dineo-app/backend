package com.dineo_backend.dineo.administration.dto;

import com.dineo_backend.dineo.plats.dto.PlatResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for chef detail view - contains complete chef information with plates and orders
 */
public class ChefDetailResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlatResponse> plates;
    private int totalOrders; // Placeholder for future order system
    private int completedOrders; // Placeholder
    private int activeOrders; // Placeholder

    public ChefDetailResponse() {}

    public ChefDetailResponse(UUID id, String firstName, String lastName, String email, String phone,
                             String address, boolean isVerified, LocalDateTime createdAt, 
                             LocalDateTime updatedAt, List<PlatResponse> plates, int totalOrders,
                             int completedOrders, int activeOrders) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.isVerified = isVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.plates = plates;
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.activeOrders = activeOrders;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PlatResponse> getPlates() { return plates; }
    public void setPlates(List<PlatResponse> plates) { this.plates = plates; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public int getActiveOrders() { return activeOrders; }
    public void setActiveOrders(int activeOrders) { this.activeOrders = activeOrders; }
}
