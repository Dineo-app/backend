package com.dineo_backend.dineo.plats.repository;

import com.dineo_backend.dineo.plats.model.ChefDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ChefDescription entity
 * Provides CRUD operations and custom queries for chef descriptions
 */
@Repository
public interface ChefDescriptionRepository extends JpaRepository<ChefDescription, UUID> {

    /**
     * Find chef description by user ID
     * 
     * @param userId The user ID to search for
     * @return Optional containing the chef description if found
     */
    Optional<ChefDescription> findByUserId(UUID userId);

    /**
     * Check if a chef description exists for a user
     * 
     * @param userId The user ID to check
     * @return true if a description exists, false otherwise
     */
    boolean existsByUserId(UUID userId);

    /**
     * Find all chef descriptions that contain a specific category
     * 
     * @param category The category to search for
     * @return List of chef descriptions containing the category
     */
    @Query("SELECT cd FROM ChefDescription cd JOIN cd.categories c WHERE c = :category")
    List<ChefDescription> findByCategory(@Param("category") String category);

    /**
     * Find all chef descriptions that contain any of the specified categories
     * 
     * @param categories List of categories to search for
     * @return List of chef descriptions containing any of the categories
     */
    @Query("SELECT DISTINCT cd FROM ChefDescription cd JOIN cd.categories c WHERE c IN :categories")
    List<ChefDescription> findByCategoriesIn(@Param("categories") List<String> categories);

    /**
     * Find all chef descriptions that contain a specific certification
     * 
     * @param certification The certification to search for
     * @return List of chef descriptions containing the certification
     */
    @Query("SELECT cd FROM ChefDescription cd JOIN cd.chefCertifications cert WHERE cert = :certification")
    List<ChefDescription> findByCertification(@Param("certification") String certification);

    /**
     * Find chef descriptions by partial description text (case-insensitive)
     * 
     * @param searchText The text to search for in descriptions
     * @return List of matching chef descriptions
     */
    @Query("SELECT cd FROM ChefDescription cd WHERE LOWER(cd.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ChefDescription> findByDescriptionContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Get all distinct categories from all chef descriptions
     * 
     * @return List of all unique categories
     */
    @Query("SELECT DISTINCT c FROM ChefDescription cd JOIN cd.categories c ORDER BY c")
    List<String> findAllDistinctCategories();

    /**
     * Get all distinct certifications from all chef descriptions
     * 
     * @return List of all unique certifications
     */
    @Query("SELECT DISTINCT cert FROM ChefDescription cd JOIN cd.chefCertifications cert ORDER BY cert")
    List<String> findAllDistinctCertifications();
}