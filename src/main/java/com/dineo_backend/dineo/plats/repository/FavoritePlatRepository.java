package com.dineo_backend.dineo.plats.repository;

import com.dineo_backend.dineo.plats.model.FavoritePlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for FavoritePlat entity
 * Provides database operations for user favorite plats
 */
@Repository
public interface FavoritePlatRepository extends JpaRepository<FavoritePlat, UUID> {

    /**
     * Find a specific favorite plat by user ID and plat ID
     * 
     * @param userId the user ID
     * @param platId the plat ID
     * @return Optional containing the favorite plat if found
     */
    Optional<FavoritePlat> findByUserIdAndPlatId(UUID userId, UUID platId);

    /**
     * Find all favorite plats for a specific user
     * 
     * @param userId the user ID
     * @return List of favorite plats for the user
     */
    List<FavoritePlat> findByUserId(UUID userId);

    /**
     * Find all users who favorited a specific plat
     * 
     * @param platId the plat ID
     * @return List of favorite plats for the plat
     */
    List<FavoritePlat> findByPlatId(UUID platId);

    /**
     * Check if a user has favorited a specific plat
     * 
     * @param userId the user ID
     * @param platId the plat ID
     * @return true if the user has favorited the plat, false otherwise
     */
    boolean existsByUserIdAndPlatId(UUID userId, UUID platId);

    /**
     * Count how many users have favorited a specific plat
     * 
     * @param platId the plat ID
     * @return number of users who favorited this plat
     */
    long countByPlatId(UUID platId);

    /**
     * Count how many plats a user has favorited
     * 
     * @param userId the user ID
     * @return number of plats favorited by this user
     */
    long countByUserId(UUID userId);

    /**
     * Delete a favorite plat by user ID and plat ID
     * 
     * @param userId the user ID
     * @param platId the plat ID
     */
    void deleteByUserIdAndPlatId(UUID userId, UUID platId);

    /**
     * Find all favorite plats for a user ordered by creation date (newest first)
     * 
     * @param userId the user ID
     * @return List of favorite plats ordered by creation date descending
     */
    List<FavoritePlat> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all favorite plats for a user ordered by creation date (oldest first)
     * 
     * @param userId the user ID
     * @return List of favorite plats ordered by creation date ascending
     */
    List<FavoritePlat> findByUserIdOrderByCreatedAtAsc(UUID userId);

    /**
     * Get the most favorited plats (plats with most favorites)
     * 
     * @param limit the maximum number of results to return
     * @return List of plat IDs ordered by favorite count descending
     */
    @Query("SELECT fp.platId, COUNT(fp) as favoriteCount " +
           "FROM FavoritePlat fp " +
           "GROUP BY fp.platId " +
           "ORDER BY favoriteCount DESC")
    List<Object[]> findMostFavoritedPlats(@Param("limit") int limit);

    /**
     * Get favorite plats for multiple users
     * 
     * @param userIds list of user IDs
     * @return List of favorite plats for the specified users
     */
    List<FavoritePlat> findByUserIdIn(List<UUID> userIds);

    /**
     * Find favorite plats for specific plats
     * 
     * @param platIds list of plat IDs
     * @return List of favorite plats for the specified plats
     */
    List<FavoritePlat> findByPlatIdIn(List<UUID> platIds);
}