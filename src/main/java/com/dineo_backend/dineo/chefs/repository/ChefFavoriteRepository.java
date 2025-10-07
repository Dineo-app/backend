package com.dineo_backend.dineo.chefs.repository;

import com.dineo_backend.dineo.chefs.model.ChefFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ChefFavorite entity
 * Provides database operations for user favorite chefs
 */
@Repository
public interface ChefFavoriteRepository extends JpaRepository<ChefFavorite, UUID> {

    /**
     * Find a specific favorite chef by user ID and chef ID
     * 
     * @param userId the user ID
     * @param chefId the chef ID
     * @return Optional containing the favorite chef if found
     */
    Optional<ChefFavorite> findByUserIdAndChefId(UUID userId, UUID chefId);

    /**
     * Find all favorite chefs for a specific user
     * 
     * @param userId the user ID
     * @return List of favorite chefs for the user
     */
    List<ChefFavorite> findByUserId(UUID userId);

    /**
     * Find all users who favorited a specific chef
     * 
     * @param chefId the chef ID
     * @return List of favorite chefs for the chef
     */
    List<ChefFavorite> findByChefId(UUID chefId);

    /**
     * Check if a user has favorited a specific chef
     * 
     * @param userId the user ID
     * @param chefId the chef ID
     * @return true if the user has favorited the chef, false otherwise
     */
    boolean existsByUserIdAndChefId(UUID userId, UUID chefId);

    /**
     * Count how many users have favorited a specific chef
     * 
     * @param chefId the chef ID
     * @return number of users who favorited this chef
     */
    long countByChefId(UUID chefId);

    /**
     * Count how many chefs a user has favorited
     * 
     * @param userId the user ID
     * @return number of chefs favorited by this user
     */
    long countByUserId(UUID userId);

    /**
     * Delete a favorite chef by user ID and chef ID
     * 
     * @param userId the user ID
     * @param chefId the chef ID
     */
    void deleteByUserIdAndChefId(UUID userId, UUID chefId);

    /**
     * Find all favorite chefs for a user ordered by creation date (newest first)
     * 
     * @param userId the user ID
     * @return List of favorite chefs ordered by creation date descending
     */
    List<ChefFavorite> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all favorite chefs for a user ordered by creation date (oldest first)
     * 
     * @param userId the user ID
     * @return List of favorite chefs ordered by creation date ascending
     */
    List<ChefFavorite> findByUserIdOrderByCreatedAtAsc(UUID userId);

    /**
     * Get the most favorited chefs (chefs with most favorites)
     * 
     * @param limit the maximum number of results to return
     * @return List of chef IDs ordered by favorite count descending
     */
    @Query("SELECT cf.chefId, COUNT(cf) as favoriteCount " +
           "FROM ChefFavorite cf " +
           "GROUP BY cf.chefId " +
           "ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoritedChefs(@Param("limit") int limit);

    /**
     * Get favorite chefs for multiple users
     * 
     * @param userIds list of user IDs
     * @return List of favorite chefs for the specified users
     */
    List<ChefFavorite> findByUserIdIn(List<UUID> userIds);

    /**
     * Find favorite chefs for specific chefs
     * 
     * @param chefIds list of chef IDs
     * @return List of favorite chefs for the specified chefs
     */
    List<ChefFavorite> findByChefIdIn(List<UUID> chefIds);

    /**
     * Get users who favorited both specific chefs (for recommendation purposes)
     * 
     * @param chefId1 first chef ID
     * @param chefId2 second chef ID
     * @return List of user IDs who favorited both chefs
     */
    @Query("SELECT cf1.userId " +
           "FROM ChefFavorite cf1 " +
           "WHERE cf1.chefId = :chefId1 " +
           "AND cf1.userId IN (" +
           "    SELECT cf2.userId " +
           "    FROM ChefFavorite cf2 " +
           "    WHERE cf2.chefId = :chefId2" +
           ")")
    List<UUID> findUsersWhoFavoritedBothChefs(@Param("chefId1") UUID chefId1, @Param("chefId2") UUID chefId2);

    /**
     * Get chefs that are commonly favorited together with a specific chef
     * 
     * @param chefId the chef ID to find similar chefs for
     * @param limit the maximum number of results to return
     * @return List of chef IDs that are commonly favorited with the given chef
     */
    @Query("SELECT cf2.chefId, COUNT(cf2) as commonCount " +
           "FROM ChefFavorite cf1 " +
           "JOIN ChefFavorite cf2 ON cf1.userId = cf2.userId " +
           "WHERE cf1.chefId = :chefId " +
           "AND cf2.chefId != :chefId " +
           "GROUP BY cf2.chefId " +
           "ORDER BY commonCount DESC")
    List<Object[]> findChefsCommonlyFavoritedWith(@Param("chefId") UUID chefId, @Param("limit") int limit);
}