package com.dineo_backend.dineo.plats.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for plat creation response
 * Returns created plat information to the client
 */
public class CreatePlatResponse {

    private UUID id;
    private UUID chefId;
    private String name;
    private String description;
    private Integer estimatedCookTime;
    private List<String> categories;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public CreatePlatResponse() {}

    // Constructor with all fields
    public CreatePlatResponse(UUID id, UUID chefId, String name, String description, 
                             Integer estimatedCookTime, List<String> categories, 
                             String imageUrl, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.chefId = chefId;
        this.name = name;
        this.description = description;
        this.estimatedCookTime = estimatedCookTime;
        this.categories = categories;
        this.imageUrl = imageUrl;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEstimatedCookTime() {
        return estimatedCookTime;
    }

    public void setEstimatedCookTime(Integer estimatedCookTime) {
        this.estimatedCookTime = estimatedCookTime;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
        return "CreatePlatResponse{" +
                "id=" + id +
                ", chefId=" + chefId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedCookTime=" + estimatedCookTime +
                ", categories=" + categories +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}