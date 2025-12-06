package com.dineo_backend.dineo.plats.dto;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for chef's plats summary with statistics
 * Contains stats for all plats on the chef-plates page
 */
public class ChefPlatsStatsResponse {

    // Summary stats
    private int totalPlats;
    private int popularPlats;
    private double totalRevenue;
    private int totalOrders;
    
    // List of plats with individual stats
    private List<PlatWithStats> plats;

    // Default constructor
    public ChefPlatsStatsResponse() {}

    // Getters and Setters
    public int getTotalPlats() {
        return totalPlats;
    }

    public void setTotalPlats(int totalPlats) {
        this.totalPlats = totalPlats;
    }

    public int getPopularPlats() {
        return popularPlats;
    }

    public void setPopularPlats(int popularPlats) {
        this.popularPlats = popularPlats;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public List<PlatWithStats> getPlats() {
        return plats;
    }

    public void setPlats(List<PlatWithStats> plats) {
        this.plats = plats;
    }

    /**
     * Inner class for plat with stats
     */
    public static class PlatWithStats {
        private UUID platId;
        private String name;
        private String description;
        private String imageUrl;
        private double price;
        private List<String> categories;
        private Integer estimatedCookTime;
        
        // Stats
        private int orders;
        private double revenue;
        private double averageRating;
        private int reviewsCount;
        private boolean isPopular;
        
        // Promotion info
        private boolean hasPromotion;
        private Double reductionValue;

        public PlatWithStats() {}

        // Getters and Setters
        public UUID getPlatId() { return platId; }
        public void setPlatId(UUID platId) { this.platId = platId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public List<String> getCategories() { return categories; }
        public void setCategories(List<String> categories) { this.categories = categories; }

        public Integer getEstimatedCookTime() { return estimatedCookTime; }
        public void setEstimatedCookTime(Integer estimatedCookTime) { this.estimatedCookTime = estimatedCookTime; }

        public int getOrders() { return orders; }
        public void setOrders(int orders) { this.orders = orders; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }

        public double getAverageRating() { return averageRating; }
        public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

        public int getReviewsCount() { return reviewsCount; }
        public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }

        @JsonProperty("isPopular")
        public boolean isPopular() { return isPopular; }
        public void setPopular(boolean popular) { isPopular = popular; }

        public boolean isHasPromotion() { return hasPromotion; }
        public void setHasPromotion(boolean hasPromotion) { this.hasPromotion = hasPromotion; }

        public Double getReductionValue() { return reductionValue; }
        public void setReductionValue(Double reductionValue) { this.reductionValue = reductionValue; }
    }
}
