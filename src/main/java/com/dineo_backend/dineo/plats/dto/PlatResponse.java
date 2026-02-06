package com.dineo_backend.dineo.plats.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Plat entity
 */
public class PlatResponse {
    private UUID id;
    private UUID chefId;
    private String name;
    private String description;
    private Integer estimatedCookTime;
    private Double price;
    private List<String> categories;
    private String imageUrl;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PlatResponse() {}

    public PlatResponse(UUID id, UUID chefId, String name, String description, Integer estimatedCookTime,
                       Double price, List<String> categories, String imageUrl, Boolean available,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.chefId = chefId;
        this.name = name;
        this.description = description;
        this.estimatedCookTime = estimatedCookTime;
        this.price = price;
        this.categories = categories;
        this.imageUrl = imageUrl;
        this.available = available;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getChefId() { return chefId; }
    public void setChefId(UUID chefId) { this.chefId = chefId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getEstimatedCookTime() { return estimatedCookTime; }
    public void setEstimatedCookTime(Integer estimatedCookTime) { this.estimatedCookTime = estimatedCookTime; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
