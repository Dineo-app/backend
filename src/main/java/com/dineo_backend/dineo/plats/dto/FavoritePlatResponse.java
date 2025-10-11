package com.dineo_backend.dineo.plats.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for favorite plat response
 */
public class FavoritePlatResponse {

    private UUID id;
    private UUID platId;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public FavoritePlatResponse() {}

    // Constructor with all fields
    public FavoritePlatResponse(UUID id, UUID platId, UUID userId, 
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.platId = platId;
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

    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
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
        return "FavoritePlatResponse{" +
                "id=" + id +
                ", platId=" + platId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}