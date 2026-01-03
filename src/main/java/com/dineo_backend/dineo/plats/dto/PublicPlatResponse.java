package com.dineo_backend.dineo.plats.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for public plate responses
 * Includes chef information for display to users
 */
public class PublicPlatResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer estimatedCookTime;
    private Double price;
    private List<String> categories;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PromotionResponse promotion;
    private ChefInfo chef;
    private Double averageRating; // Average rating from reviews
    private Double distanceKm; // Distance from user location in kilometers (optional)
    private Boolean isChefOpen; // Whether the chef is currently open for orders

    // Nested class for chef information
    public static class ChefInfo {
        private UUID id;
        private String firstName;
        private String lastName;
        private String chefCoverImg;
        private Double averageRating;

        public ChefInfo() {}

        public ChefInfo(UUID id, String firstName, String lastName, String chefCoverImg) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.chefCoverImg = chefCoverImg;
        }

        public ChefInfo(UUID id, String firstName, String lastName, String chefCoverImg, Double averageRating) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.chefCoverImg = chefCoverImg;
            this.averageRating = averageRating;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
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

        public String getChefCoverImg() {
            return chefCoverImg;
        }

        public void setChefCoverImg(String chefCoverImg) {
            this.chefCoverImg = chefCoverImg;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }
    }

    // Constructors
    public PublicPlatResponse() {}

    public PublicPlatResponse(UUID id, String name, String description, Integer estimatedCookTime,
                             Double price, List<String> categories, String imageUrl,
                             LocalDateTime createdAt, LocalDateTime updatedAt,
                             PromotionResponse promotion, ChefInfo chef) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.estimatedCookTime = estimatedCookTime;
        this.price = price;
        this.categories = categories;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.promotion = promotion;
        this.chef = chef;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public PromotionResponse getPromotion() {
        return promotion;
    }

    public void setPromotion(PromotionResponse promotion) {
        this.promotion = promotion;
    }

    public ChefInfo getChef() {
        return chef;
    }

    public void setChef(ChefInfo chef) {
        this.chef = chef;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Boolean getIsChefOpen() {
        return isChefOpen;
    }

    public void setIsChefOpen(Boolean isChefOpen) {
        this.isChefOpen = isChefOpen;
    }

    @Override
    public String toString() {
        return "PublicPlatResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedCookTime=" + estimatedCookTime +
                ", price=" + price +
                ", categories=" + categories +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", promotion=" + promotion +
                ", chef=" + chef +
                ", averageRating=" + averageRating +
                '}';
    }
}
