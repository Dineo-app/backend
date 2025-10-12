package com.dineo_backend.dineo.plats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for plat review response with user information
 */
public class PlatReviewResponse {
    
    @JsonProperty("reviewId")
    private UUID reviewId;
    
    @JsonProperty("platId")
    private UUID platId;
    
    @JsonProperty("userId")
    private UUID userId;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("reviewText")
    private String reviewText;
    
    @JsonProperty("rate")
    private Integer rate;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Default constructor
    public PlatReviewResponse() {
    }
    
    // Constructor with all fields
    public PlatReviewResponse(UUID reviewId, UUID platId, UUID userId, String userName, 
                             String reviewText, Integer rate, LocalDateTime createdAt, 
                             LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.platId = platId;
        this.userId = userId;
        this.userName = userName;
        this.reviewText = reviewText;
        this.rate = rate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public UUID getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(UUID reviewId) {
        this.reviewId = reviewId;
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
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getReviewText() {
        return reviewText;
    }
    
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    
    public Integer getRate() {
        return rate;
    }
    
    public void setRate(Integer rate) {
        this.rate = rate;
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
        return "PlatReviewResponse{" +
                "reviewId=" + reviewId +
                ", platId=" + platId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", reviewText='" + reviewText + '\'' +
                ", rate=" + rate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
