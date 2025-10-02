package com.dineo_backend.dineo.plats.repository;

import com.dineo_backend.dineo.plats.model.PlatReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PlatReview entity
 * Provides CRUD operations and custom queries for plat reviews
 */
@Repository
public interface PlatReviewRepository extends JpaRepository<PlatReview, UUID> {

    /**
     * Find all reviews for a specific plat
     * 
     * @param platId The plat ID to search for
     * @return List of reviews for the plat
     */
    List<PlatReview> findByPlatId(UUID platId);

    /**
     * Find all reviews by a specific user
     * 
     * @param userId The user ID to search for
     * @return List of reviews created by the user
     */
    List<PlatReview> findByUserId(UUID userId);

    /**
     * Find a review by plat ID and user ID (to check if user already reviewed)
     * 
     * @param platId The plat ID
     * @param userId The user ID
     * @return Optional containing the review if found
     */
    Optional<PlatReview> findByPlatIdAndUserId(UUID platId, UUID userId);

    /**
     * Check if a user has already reviewed a specific plat
     * 
     * @param platId The plat ID
     * @param userId The user ID
     * @return true if review exists, false otherwise
     */
    boolean existsByPlatIdAndUserId(UUID platId, UUID userId);

    /**
     * Find reviews by rating
     * 
     * @param rate The rating to search for (1-5)
     * @return List of reviews with the specified rating
     */
    List<PlatReview> findByRate(Integer rate);

    /**
     * Find reviews with rating greater than or equal to specified value
     * 
     * @param minRate Minimum rating
     * @return List of reviews with rating >= minRate
     */
    List<PlatReview> findByRateGreaterThanEqual(Integer minRate);

    /**
     * Find reviews with rating less than or equal to specified value
     * 
     * @param maxRate Maximum rating
     * @return List of reviews with rating <= maxRate
     */
    List<PlatReview> findByRateLessThanEqual(Integer maxRate);

    /**
     * Find reviews for a plat with rating between specified values
     * 
     * @param platId The plat ID
     * @param minRate Minimum rating
     * @param maxRate Maximum rating
     * @return List of reviews for the plat within the rating range
     */
    List<PlatReview> findByPlatIdAndRateBetween(UUID platId, Integer minRate, Integer maxRate);

    /**
     * Calculate average rating for a specific plat
     * 
     * @param platId The plat ID
     * @return Average rating as Double, null if no reviews
     */
    @Query("SELECT AVG(pr.rate) FROM PlatReview pr WHERE pr.platId = :platId")
    Double findAverageRatingByPlatId(@Param("platId") UUID platId);

    /**
     * Count total reviews for a specific plat
     * 
     * @param platId The plat ID
     * @return Number of reviews for the plat
     */
    long countByPlatId(UUID platId);

    /**
     * Count total reviews by a specific user
     * 
     * @param userId The user ID
     * @return Number of reviews created by the user
     */
    long countByUserId(UUID userId);

    /**
     * Find reviews for a plat ordered by rating descending
     * 
     * @param platId The plat ID
     * @return List of reviews ordered by highest rating first
     */
    List<PlatReview> findByPlatIdOrderByRateDesc(UUID platId);

    /**
     * Find reviews for a plat ordered by creation date descending (newest first)
     * 
     * @param platId The plat ID
     * @return List of reviews ordered by newest first
     */
    List<PlatReview> findByPlatIdOrderByCreatedAtDesc(UUID platId);

    /**
     * Search reviews by review text containing search term (case-insensitive)
     * 
     * @param searchText The text to search for
     * @return List of reviews containing the search text
     */
    @Query("SELECT pr FROM PlatReview pr WHERE LOWER(pr.reviewText) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<PlatReview> findByReviewTextContainingIgnoreCase(@Param("searchText") String searchText);

    /**
     * Get rating distribution for a specific plat
     * 
     * @param platId The plat ID
     * @return List of Object arrays containing [rating, count]
     */
    @Query("SELECT pr.rate, COUNT(pr) FROM PlatReview pr WHERE pr.platId = :platId GROUP BY pr.rate ORDER BY pr.rate")
    List<Object[]> getRatingDistributionByPlatId(@Param("platId") UUID platId);
}