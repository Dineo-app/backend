package com.dineo_backend.dineo.plats.repository;

import com.dineo_backend.dineo.plats.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing ingredients
 */
@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {
    
    /**
     * Find all ingredients for a specific plate
     * @param platId the ID of the plate
     * @return list of ingredients for the plate
     */
    List<Ingredient> findByPlatId(UUID platId);
    
    /**
     * Delete all ingredients for a specific plate
     * @param platId the ID of the plate
     */
    void deleteByPlatId(UUID platId);
}
