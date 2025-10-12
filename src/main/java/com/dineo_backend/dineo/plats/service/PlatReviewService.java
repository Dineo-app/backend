package com.dineo_backend.dineo.plats.service;

import com.dineo_backend.dineo.plats.dto.AddPlatReviewRequest;
import com.dineo_backend.dineo.plats.dto.PlatReviewResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for plat review operations
 */
public interface PlatReviewService {
    
    /**
     * Add a review for a plat
     * 
     * @param userId The ID of the user adding the review
     * @param request The review request containing platId, reviewText, and rate
     * @return The created review response with user information
     * @throws RuntimeException if plat not found or user already reviewed
     */
    PlatReviewResponse addReview(UUID userId, AddPlatReviewRequest request);
    
    /**
     * Get all reviews for a specific plat with user information
     * 
     * @param platId The ID of the plat
     * @return List of reviews with user information
     */
    List<PlatReviewResponse> getPlatReviews(UUID platId);
    
    /**
     * Get all reviews by a specific user
     * 
     * @param userId The ID of the user
     * @return List of reviews created by the user
     */
    List<PlatReviewResponse> getUserReviews(UUID userId);
    
    /**
     * Check if a user has already reviewed a plat
     * 
     * @param userId The ID of the user
     * @param platId The ID of the plat
     * @return true if user has reviewed, false otherwise
     */
    boolean hasUserReviewedPlat(UUID userId, UUID platId);
}
