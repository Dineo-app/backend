package com.dineo_backend.dineo.plats.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for adding a review to a plat
 */
public class AddPlatReviewRequest {
    
    @NotNull(message = "L'identifiant du plat est obligatoire")
    private UUID platId;
    
    @NotBlank(message = "Le texte de l'avis est obligatoire")
    @Size(max = 1000, message = "L'avis ne peut pas dépasser 1000 caractères")
    private String reviewText;
    
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note doit être au minimum 1")
    @Max(value = 5, message = "La note ne peut pas dépasser 5")
    private Integer rate;
    
    // Default constructor
    public AddPlatReviewRequest() {
    }
    
    // Constructor with all fields
    public AddPlatReviewRequest(UUID platId, String reviewText, Integer rate) {
        this.platId = platId;
        this.reviewText = reviewText;
        this.rate = rate;
    }
    
    // Getters and Setters
    public UUID getPlatId() {
        return platId;
    }
    
    public void setPlatId(UUID platId) {
        this.platId = platId;
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
    
    @Override
    public String toString() {
        return "AddPlatReviewRequest{" +
                "platId=" + platId +
                ", reviewText='" + reviewText + '\'' +
                ", rate=" + rate +
                '}';
    }
}
