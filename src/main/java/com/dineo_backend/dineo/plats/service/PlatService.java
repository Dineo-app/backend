package com.dineo_backend.dineo.plats.service;

import com.dineo_backend.dineo.plats.dto.CreatePlatRequest;
import com.dineo_backend.dineo.plats.dto.CreatePlatResponse;
import com.dineo_backend.dineo.plats.dto.UpdatePlatRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Plat operations
 * Handles plat creation, management, and business logic
 */
public interface PlatService {
    
    /**
     * Create a new plat with image upload
     * Only users with PROVIDER role can create plats
     * 
     * @param request the plat creation request
     * @param imageFile the plat image file
     * @param chefUserId the chef user ID (from JWT token)
     * @return the created plat response
     * @throws RuntimeException if user is not a chef or creation fails
     */
    CreatePlatResponse createPlat(CreatePlatRequest request, MultipartFile imageFile, UUID chefUserId);
    
    /**
     * Get all plats created by a specific chef
     * Only chefs can retrieve their own plats
     * 
     * @param chefUserId the chef user ID
     * @return list of plats created by the chef
     * @throws RuntimeException if user is not a chef
     */
    List<CreatePlatResponse> getChefPlats(UUID chefUserId);
    
    /**
     * Get all unique categories from a chef's plats
     * Returns a sorted list of distinct categories
     * 
     * @param chefUserId the chef user ID
     * @return list of unique category names
     * @throws RuntimeException if user is not a chef
     */
    List<String> getChefCategories(UUID chefUserId);
    
    /**
     * Delete a plat by ID
     * Only the chef who created the plat can delete it
     * Validates that the user has PROVIDER role and owns the plat
     * 
     * @param platId the plat ID to delete
     * @param chefUserId the chef user ID (from JWT token)
     * @throws RuntimeException if user is not a chef or doesn't own the plat
     */
    void deletePlat(UUID platId, UUID chefUserId);
    
    /**
     * Get a single plat by ID
     * Returns plat details for any authenticated user
     * 
     * @param platId the plat ID to retrieve
     * @return the plat details
     * @throws RuntimeException if plat not found
     */
    CreatePlatResponse getPlatById(UUID platId);
    
    /**
     * Update an existing plat
     * Only the chef who created the plat can update it
     * If no new image is provided, the existing image URL is kept
     * 
     * @param platId the plat ID to update
     * @param request the update request with fields to change
     * @param imageFile optional new image file
     * @param chefUserId the chef user ID (from JWT token)
     * @return the updated plat response
     * @throws RuntimeException if user is not a chef or doesn't own the plat
     */
    CreatePlatResponse updatePlat(UUID platId, UpdatePlatRequest request, MultipartFile imageFile, UUID chefUserId);
    
    /**
     * Validate that user has PROVIDER role (is a chef)
     * 
     * @param userId the user ID to check
     * @return true if user is a chef, false otherwise
     */
    boolean isUserAChef(UUID userId);
}