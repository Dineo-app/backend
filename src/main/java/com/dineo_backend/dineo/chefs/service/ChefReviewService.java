package com.dineo_backend.dineo.chefs.service;

import com.dineo_backend.dineo.chefs.dto.AddChefReviewRequest;
import com.dineo_backend.dineo.chefs.dto.ChefReviewResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for chef review operations
 */
public interface ChefReviewService {
    
    /**
     * Add a review for a chef
     * 
     * @param userId The ID of the user adding the review
     * @param request The review request containing chefId, reviewText, and rate
     * @return The created review response with user information
     * @throws RuntimeException if chef not found or user already reviewed
     */
    ChefReviewResponse addReview(UUID userId, AddChefReviewRequest request);
    
    /**
     * Get all reviews for a specific chef with user information
     * 
     * @param chefId The ID of the chef
     * @return List of reviews with user information
     */
    List<ChefReviewResponse> getChefReviews(UUID chefId);
    
    /**
     * Get all reviews by a specific user
     * 
     * @param userId The ID of the user
     * @return List of reviews created by the user
     */
    List<ChefReviewResponse> getUserReviews(UUID userId);
    
    /**
     * Check if a user has already reviewed a chef
     * 
     * @param userId The ID of the user
     * @param chefId The ID of the chef
     * @return true if user has reviewed, false otherwise
     */
    boolean hasUserReviewedChef(UUID userId, UUID chefId);
}