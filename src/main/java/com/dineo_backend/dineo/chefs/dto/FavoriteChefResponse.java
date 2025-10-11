package com.dineo_backend.dineo.chefs.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for favorite chef response
 */
public class FavoriteChefResponse {

    private UUID id;
    private UUID chefId;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public FavoriteChefResponse() {}

    // Constructor with all fields
    public FavoriteChefResponse(UUID id, UUID chefId, UUID userId, 
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.chefId = chefId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "FavoriteChefResponse{" +
                "id=" + id +
                ", chefId=" + chefId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}