package com.dineo_backend.dineo.chefs.service;

import com.dineo_backend.dineo.chefs.dto.AddFavoriteChefRequest;
import com.dineo_backend.dineo.chefs.dto.FavoriteChefResponse;
import com.dineo_backend.dineo.chefs.dto.FavoriteChefWithDetailsResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for favorite chef operations
 */
public interface FavoriteChefService {

    /**
     * Add a chef to user's favorites
     * 
     * @param userId The ID of the user
     * @param request The request containing the chef ID
     * @return The created favorite chef response
     */
    FavoriteChefResponse addChefToFavorites(UUID userId, AddFavoriteChefRequest request);

    /**
     * Remove a chef from user's favorites
     * 
     * @param userId The ID of the user
     * @param chefId The ID of the chef to remove from favorites
     */
    void removeChefFromFavorites(UUID userId, UUID chefId);

    /**
     * Get all favorite chefs for a user
     * 
     * @param userId The ID of the user
     * @return List of user's favorite chefs
     */
    List<FavoriteChefResponse> getUserFavoriteChefs(UUID userId);

    /**
     * Get all favorite chefs for a user with complete chef details
     * 
     * @param userId The ID of the user
     * @return List of user's favorite chefs with complete details
     */
    List<FavoriteChefWithDetailsResponse> getUserFavoriteChefsWithDetails(UUID userId);

    /**
     * Check if a chef is in user's favorites
     * 
     * @param userId The ID of the user
     * @param chefId The ID of the chef
     * @return true if the chef is in favorites, false otherwise
     */
    boolean isChefInFavorites(UUID userId, UUID chefId);
}