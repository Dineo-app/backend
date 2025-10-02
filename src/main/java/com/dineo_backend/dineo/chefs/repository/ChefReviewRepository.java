package com.dineo_backend.dineo.chefs.repository;

import com.dineo_backend.dineo.chefs.model.ChefReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ChefReview entity
 * Provides CRUD operations and custom queries for chef reviews
 */
@Repository
public interface ChefReviewRepository extends JpaRepository<ChefReview, UUID> {

    /**
     * Find all reviews for a specific chef
     * 
     * @param chefId The chef ID to search for
     * @return List of reviews for the chef
     */
    List<ChefReview> findByChefId(UUID chefId);

    /**
     * Find all reviews by a specific user
     * 
     * @param userId The user ID to search for
     * @return List of reviews created by the user
     */
    List<ChefReview> findByUserId(UUID userId);

    /**
     * Find a review by chef ID and user ID (to check if user already reviewed)
     * 
     * @param chefId The chef ID
     * @param userId The user ID
     * @return Optional containing the review if found
     */
    Optional<ChefReview> findByChefIdAndUserId(UUID chefId, UUID userId);

    /**
     * Check if a user has already reviewed a specific chef
     * 
     * @param chefId The chef ID
     * @param userId The user ID
     * @return true if review exists, false otherwise
     */
    boolean existsByChefIdAndUserId(UUID chefId, UUID userId);

    /**
     * Find reviews by rating
     * 
     * @param rate The rating to search for (1-5)
     * @return List of reviews with the specified rating
     */
    List<ChefReview> findByRate(Integer rate);

    /**
     * Find reviews with rating greater than or equal to specified value
     * 
     * @param minRate Minimum rating
     * @return List of reviews with rating >= minRate
     */
    List<ChefReview> findByRateGreaterThanEqual(Integer minRate);

    /**
     * Find reviews with rating less than or equal to specified value
     * 
     * @param maxRate Maximum rating
     * @return List of reviews with rating <= maxRate
     */
    List<ChefReview> findByRateLessThanEqual(Integer maxRate);

    /**
     * Find reviews for a chef with rating between specified values
     * 
     * @param chefId The chef ID
     * @param minRate Minimum rating
     * @param maxRate Maximum rating
     * @return List of reviews for the chef within the rating range
     */
    List<ChefReview> findByChefIdAndRateBetween(UUID chefId, Integer minRate, Integer maxRate);

    /**
     * Calculate average rating for a specific chef
     * 
     * @param chefId The chef ID
     * @return Average rating as Double, null if no reviews
     */
    @Query("SELECT AVG(cr.rate) FROM ChefReview cr WHERE cr.chefId = :chefId")
    Double findAverageRatingByChefId(@Param("chefId") UUID chefId);

    /**
     * Count total reviews for a specific chef
     * 
     * @param chefId The chef ID
     * @return Number of reviews for the chef
     */
    long countByChefId(UUID chefId);

    /**
     * Count total reviews by a specific user
     * 
     * @param userId The user ID
     * @return Number of reviews created by the user
     */
    long countByUserId(UUID userId);

    /**
     * Find reviews for a chef ordered by rating descending
     * 
     * @param chefId The chef ID
     * @return List of reviews ordered by highest rating first
     */
    List<ChefReview> findByChefIdOrderByRateDesc(UUID chefId);

    /**
     * Find reviews for a chef ordered by creation date descending (newest first)
     * 
     * @param chefId The chef ID
     * @return List of reviews ordered by newest first
     */
    List<ChefReview> findByChefIdOrderByCreatedAtDesc(UUID chefId);

    /**
     * Search reviews by review text containing search term (case-insensitive)
     * 
     * @param searchText The text to search for
     * @return List of reviews containing the search text
     */
    @Query("SELECT cr FROM ChefReview cr WHERE LOWER(cr.reviewText) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<ChefReview> findByReviewTextContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Get rating distribution for a specific chef
     * 
     * @param chefId The chef ID
     * @return List of Object arrays containing [rating, count]
     */
    @Query("SELECT cr.rate, COUNT(cr) FROM ChefReview cr WHERE cr.chefId = :chefId GROUP BY cr.rate ORDER BY cr.rate")
    List<Object[]> getRatingDistributionByChefId(@Param("chefId") UUID chefId);

    /**
     * Find top-rated chefs based on average rating
     * 
     * @param limit Maximum number of chefs to return
     * @return List of Object arrays containing [chefId, averageRating, reviewCount]
     */
    @Query(value = "SELECT cr.chef_id, AVG(cr.rate) as avg_rating, COUNT(cr.id) as review_count " +
           "FROM chef_reviews cr " +
           "GROUP BY cr.chef_id " +
           "HAVING COUNT(cr.id) >= 3 " +
           "ORDER BY avg_rating DESC, review_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findTopRatedChefs(@Param("limit") int limit);

    /**
     * Find chefs with most reviews
     * 
     * @param limit Maximum number of chefs to return
     * @return List of Object arrays containing [chefId, reviewCount, averageRating]
     */
    @Query("SELECT cr.chefId, COUNT(cr), AVG(cr.rate) " +
           "FROM ChefReview cr " +
           "GROUP BY cr.chefId " +
           "ORDER BY COUNT(cr) DESC")
    List<Object[]> findChefsWithMostReviews(@Param("limit") int limit);
}