package com.dineo_backend.dineo.plats.service;

import com.dineo_backend.dineo.plats.dto.IngredientRequest;
import com.dineo_backend.dineo.plats.dto.IngredientResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing ingredients
 */
public interface IngredientService {
    
    /**
     * Create a new ingredient for a plate
     * @param platId the ID of the plate
     * @param request the ingredient data
     * @return the created ingredient
     */
    IngredientResponse createIngredient(UUID platId, IngredientRequest request);
    
    /**
     * Get all ingredients for a plate
     * @param platId the ID of the plate
     * @return list of ingredients
     */
    List<IngredientResponse> getIngredientsByPlatId(UUID platId);
    
    /**
     * Update an ingredient
     * @param ingredientId the ID of the ingredient
     * @param request the updated ingredient data
     * @return the updated ingredient
     */
    IngredientResponse updateIngredient(UUID ingredientId, IngredientRequest request);
    
    /**
     * Delete an ingredient
     * @param ingredientId the ID of the ingredient
     */
    void deleteIngredient(UUID ingredientId);
    
    /**
     * Delete all ingredients for a plate
     * @param platId the ID of the plate
     */
    void deleteIngredientsByPlatId(UUID platId);
}
