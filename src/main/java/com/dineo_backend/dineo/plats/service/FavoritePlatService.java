package com.dineo_backend.dineo.plats.service;

import com.dineo_backend.dineo.plats.dto.AddFavoritePlatRequest;
import com.dineo_backend.dineo.plats.dto.FavoritePlatResponse;
import com.dineo_backend.dineo.plats.dto.FavoritePlatWithDetailsResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for favorite plat operations
 */
public interface FavoritePlatService {

    /**
     * Add a plat to user's favorites
     * 
     * @param userId The ID of the user
     * @param request The request containing the plat ID
     * @return The created favorite plat response
     */
    FavoritePlatResponse addPlatToFavorites(UUID userId, AddFavoritePlatRequest request);

    /**
     * Remove a plat from user's favorites
     * 
     * @param userId The ID of the user
     * @param platId The ID of the plat to remove from favorites
     */
    void removePlatFromFavorites(UUID userId, UUID platId);

    /**
     * Get all favorite plats for a user
     * 
     * @param userId The ID of the user
     * @return List of user's favorite plats
     */
    List<FavoritePlatResponse> getUserFavorites(UUID userId);

    /**
     * Get all favorite plats for a user with complete plat details
     * 
     * @param userId The ID of the user
     * @return List of user's favorite plats with complete details
     */
    List<FavoritePlatWithDetailsResponse> getUserFavoritesWithDetails(UUID userId);

    /**
     * Check if a plat is in user's favorites
     * 
     * @param userId The ID of the user
     * @param platId The ID of the plat
     * @return true if the plat is in favorites, false otherwise
     */
    boolean isPlatInFavorites(UUID userId, UUID platId);
}