package com.dineo_backend.dineo.plats.repository;

import com.dineo_backend.dineo.plats.model.Plat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Plat entity
 * Provides CRUD operations and custom queries for dishes/plates
 */
@Repository
public interface PlatRepository extends JpaRepository<Plat, UUID> {

    /**
     * Find all plats by chef ID
     * 
     * @param chefId The chef ID to search for
     * @return List of plats created by the chef
     */
    List<Plat> findByChefId(UUID chefId);

    /**
     * Find all plats by chef ID ordered by creation date (newest first)
     * 
     * @param chefId The chef ID to search for
     * @return List of plats created by the chef, ordered by creation date descending
     */
    List<Plat> findByChefIdOrderByCreatedAtDesc(UUID chefId);

    /**
     * Find plat by ID and chef ID (for authorization purposes)
     * 
     * @param id The plat ID
     * @param chefId The chef ID
     * @return Optional containing the plat if found and belongs to the chef
     */
    Optional<Plat> findByIdAndChefId(UUID id, UUID chefId);

    /**
     * Check if a plat exists for a specific chef
     * 
     * @param id The plat ID
     * @param chefId The chef ID
     * @return true if the plat exists and belongs to the chef
     */
    boolean existsByIdAndChefId(UUID id, UUID chefId);

    /**
     * Find all plats that contain a specific category
     * 
     * @param category The category to search for
     * @return List of plats containing the category
     */
    @Query("SELECT p FROM Plat p JOIN p.categories c WHERE c = :category")
    List<Plat> findByCategory(@Param("category") String category);

    /**
     * Find all plats that contain any of the specified categories
     * 
     * @param categories List of categories to search for
     * @return List of plats containing any of the categories
     */
    @Query("SELECT DISTINCT p FROM Plat p JOIN p.categories c WHERE c IN :categories")
    List<Plat> findByCategoriesIn(@Param("categories") List<String> categories);

    /**
     * Find plats by name containing search text (case-insensitive)
     * 
     * @param searchText The text to search for in plat names
     * @return List of matching plats
     */
    @Query("SELECT p FROM Plat p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Plat> findByNameContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Find plats by description containing search text (case-insensitive)
     * 
     * @param searchText The text to search for in descriptions
     * @return List of matching plats
     */
    @Query("SELECT p FROM Plat p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Plat> findByDescriptionContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Find plats by estimated cooking time range
     * 
     * @param minTime Minimum cooking time in minutes
     * @param maxTime Maximum cooking time in minutes
     * @return List of plats within the time range
     */
    List<Plat> findByEstimatedCookTimeBetween(Integer minTime, Integer maxTime);

    /**
     * Find plats with cooking time less than or equal to specified time
     * 
     * @param maxTime Maximum cooking time in minutes
     * @return List of plats with cooking time <= maxTime
     */
    List<Plat> findByEstimatedCookTimeLessThanEqual(Integer maxTime);

    /**
     * Get all distinct categories from all plats
     * 
     * @return List of all unique categories
     */
    @Query("SELECT DISTINCT c FROM Plat p JOIN p.categories c ORDER BY c")
    List<String> findAllDistinctCategories();

    /**
     * Count plats by chef ID
     * 
     * @param chefId The chef ID
     * @return Number of plats created by the chef
     */
    long countByChefId(UUID chefId);

    /**
     * Find plats by chef ID and category
     * 
     * @param chefId The chef ID
     * @param category The category to filter by
     * @return List of plats by chef in the specified category
     */
    @Query("SELECT p FROM Plat p JOIN p.categories c WHERE p.chefId = :chefId AND c = :category")
    List<Plat> findByChefIdAndCategory(@Param("chefId") UUID chefId, @Param("category") String category);

    /**
     * Search plats by name or description (case-insensitive)
     * 
     * @param searchText The text to search for
     * @return List of plats matching the search criteria
     */
    @Query("SELECT p FROM Plat p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Plat> searchByNameOrDescription(@Param("searchText") String searchText);

    /**
     * Find all available plats (for public access)
     * 
     * @return List of all available plats
     */
    List<Plat> findByAvailableTrue();

    /**
     * Find available plat by ID (for public access)
     * 
     * @param id The plat ID
     * @return Optional containing the plat if found and available
     */
    Optional<Plat> findByIdAndAvailableTrue(UUID id);
}