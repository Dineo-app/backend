package com.dineo_backend.dineo.plats.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for favorite plat response with complete plat details
 * Includes both favorite information and full plat details
 */
public class FavoritePlatWithDetailsResponse {

    // Favorite information
    private UUID favoriteId;
    private LocalDateTime favoritedAt;

    // Complete plat information
    private UUID platId;
    private UUID chefId;
    private String name;
    private String description;
    private Integer estimatedCookTime;
    private List<String> categories;
    private String imageUrl;
    private LocalDateTime platCreatedAt;
    private LocalDateTime platUpdatedAt;

    // Additional fields for UI display (matching home page cards)
    private Double price;
    private Double averageRating;
    private String chefFirstName;
    private String chefLastName;
    private PromotionResponse promotion;

    // Default constructor
    public FavoritePlatWithDetailsResponse() {}

    // Constructor with all fields
    public FavoritePlatWithDetailsResponse(UUID favoriteId, LocalDateTime favoritedAt,
                                         UUID platId, UUID chefId, String name, String description,
                                         Integer estimatedCookTime, List<String> categories,
                                         String imageUrl, LocalDateTime platCreatedAt,
                                         LocalDateTime platUpdatedAt, Double price,
                                         Double averageRating, String chefFirstName,
                                         String chefLastName, PromotionResponse promotion) {
        this.favoriteId = favoriteId;
        this.favoritedAt = favoritedAt;
        this.platId = platId;
        this.chefId = chefId;
        this.name = name;
        this.description = description;
        this.estimatedCookTime = estimatedCookTime;
        this.categories = categories;
        this.imageUrl = imageUrl;
        this.platCreatedAt = platCreatedAt;
        this.platUpdatedAt = platUpdatedAt;
        this.price = price;
        this.averageRating = averageRating;
        this.chefFirstName = chefFirstName;
        this.chefLastName = chefLastName;
        this.promotion = promotion;
    }

    // Getters and Setters
    public UUID getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(UUID favoriteId) {
        this.favoriteId = favoriteId;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }

    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
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

    public LocalDateTime getPlatCreatedAt() {
        return platCreatedAt;
    }

    public void setPlatCreatedAt(LocalDateTime platCreatedAt) {
        this.platCreatedAt = platCreatedAt;
    }

    public LocalDateTime getPlatUpdatedAt() {
        return platUpdatedAt;
    }

    public void setPlatUpdatedAt(LocalDateTime platUpdatedAt) {
        this.platUpdatedAt = platUpdatedAt;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public String getChefFirstName() {
        return chefFirstName;
    }

    public void setChefFirstName(String chefFirstName) {
        this.chefFirstName = chefFirstName;
    }

    public String getChefLastName() {
        return chefLastName;
    }

    public void setChefLastName(String chefLastName) {
        this.chefLastName = chefLastName;
    }

    public PromotionResponse getPromotion() {
        return promotion;
    }

    public void setPromotion(PromotionResponse promotion) {
        this.promotion = promotion;
    }

    @Override
    public String toString() {
        return "FavoritePlatWithDetailsResponse{" +
                "favoriteId=" + favoriteId +
                ", favoritedAt=" + favoritedAt +
                ", platId=" + platId +
                ", chefId=" + chefId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedCookTime=" + estimatedCookTime +
                ", categories=" + categories +
                ", imageUrl='" + imageUrl + '\'' +
                ", platCreatedAt=" + platCreatedAt +
                ", platUpdatedAt=" + platUpdatedAt +
                ", price=" + price +
                ", averageRating=" + averageRating +
                ", chefFirstName='" + chefFirstName + '\'' +
                ", chefLastName='" + chefLastName + '\'' +
                ", promotion=" + promotion +
                '}';
    }
}