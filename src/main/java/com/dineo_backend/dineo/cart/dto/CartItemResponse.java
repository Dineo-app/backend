package com.dineo_backend.dineo.cart.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for cart item response
 * Includes plat details for display
 */
public class CartItemResponse {

    private UUID id;
    private UUID platId;
    private String platName;
    private String platDescription;
    private Double platPrice;
    private String platImageUrl;
    private Integer quantity;
    private Double totalPrice;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    private String chefName;
    private UUID chefId;
    private PromotionInfo promotion;

    // Nested class for promotion information
    public static class PromotionInfo {
        private UUID id;
        private Double discountPercentage;
        private Double originalPrice;
        private Double discountedPrice;

        public PromotionInfo() {
        }

        public PromotionInfo(UUID id, Double discountPercentage, Double originalPrice, Double discountedPrice) {
            this.id = id;
            this.discountPercentage = discountPercentage;
            this.originalPrice = originalPrice;
            this.discountedPrice = discountedPrice;
        }

        // Getters and Setters
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Double getDiscountPercentage() {
            return discountPercentage;
        }

        public void setDiscountPercentage(Double discountPercentage) {
            this.discountPercentage = discountPercentage;
        }

        public Double getOriginalPrice() {
            return originalPrice;
        }

        public void setOriginalPrice(Double originalPrice) {
            this.originalPrice = originalPrice;
        }

        public Double getDiscountedPrice() {
            return discountedPrice;
        }

        public void setDiscountedPrice(Double discountedPrice) {
            this.discountedPrice = discountedPrice;
        }
    }

    // Constructors
    public CartItemResponse() {
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

    public String getPlatName() {
        return platName;
    }

    public void setPlatName(String platName) {
        this.platName = platName;
    }

    public String getPlatDescription() {
        return platDescription;
    }

    public void setPlatDescription(String platDescription) {
        this.platDescription = platDescription;
    }

    public Double getPlatPrice() {
        return platPrice;
    }

    public void setPlatPrice(Double platPrice) {
        this.platPrice = platPrice;
    }

    public String getPlatImageUrl() {
        return platImageUrl;
    }

    public void setPlatImageUrl(String platImageUrl) {
        this.platImageUrl = platImageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getChefName() {
        return chefName;
    }

    public void setChefName(String chefName) {
        this.chefName = chefName;
    }

    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

    public PromotionInfo getPromotion() {
        return promotion;
    }

    public void setPromotion(PromotionInfo promotion) {
        this.promotion = promotion;
    }
}
