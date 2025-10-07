package com.dineo_backend.dineo.plats.repository;

import com.dineo_backend.dineo.plats.model.PromotionPlat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PromotionPlat entity
 * Provides database operations for plat promotions and discounts
 */
@Repository
public interface PromotionPlatRepository extends JpaRepository<PromotionPlat, UUID> {

    /**
     * Find active promotion for a specific plat
     * 
     * @param platId the plat ID
     * @return Optional containing the active promotion if found
     */
    @Query("SELECT pp FROM PromotionPlat pp WHERE pp.platId = :platId " +
           "AND pp.isActive = true AND pp.reductionEnds > :now")
    Optional<PromotionPlat> findActivePromotionByPlatId(@Param("platId") UUID platId, @Param("now") LocalDateTime now);

    /**
     * Find all promotions for a specific plat (active and inactive)
     * 
     * @param platId the plat ID
     * @return List of promotions for the plat
     */
    List<PromotionPlat> findByPlatIdOrderByCreatedAtDesc(UUID platId);

    /**
     * Find all active promotions
     * 
     * @param now current date and time
     * @return List of currently active promotions
     */
    @Query("SELECT pp FROM PromotionPlat pp WHERE pp.isActive = true AND pp.reductionEnds > :now")
    List<PromotionPlat> findAllActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Find all expired promotions that are still marked as active
     * 
     * @param now current date and time
     * @return List of expired but still active promotions
     */
    @Query("SELECT pp FROM PromotionPlat pp WHERE pp.isActive = true AND pp.reductionEnds <= :now")
    List<PromotionPlat> findExpiredActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Find promotions by plat IDs (for batch operations)
     * 
     * @param platIds list of plat IDs
     * @param now current date and time
     * @return List of active promotions for the specified plats
     */
    @Query("SELECT pp FROM PromotionPlat pp WHERE pp.platId IN :platIds " +
           "AND pp.isActive = true AND pp.reductionEnds > :now")
    List<PromotionPlat> findActivePromotionsByPlatIds(@Param("platIds") List<UUID> platIds, @Param("now") LocalDateTime now);

    /**
     * Find promotions ending soon (within specified hours)
     * 
     * @param now current date and time
     * @param endingSoon date and time threshold for "ending soon"
     * @return List of promotions ending soon
     */
    @Query("SELECT pp FROM PromotionPlat pp WHERE pp.isActive = true " +
           "AND pp.reductionEnds > :now AND pp.reductionEnds <= :endingSoon")
    List<PromotionPlat> findPromotionsEndingSoon(@Param("now") LocalDateTime now, @Param("endingSoon") LocalDateTime endingSoon);

    /**
     * Check if a plat has an active promotion
     * 
     * @param platId the plat ID
     * @param now current date and time
     * @return true if plat has an active promotion
     */
    @Query("SELECT COUNT(pp) > 0 FROM PromotionPlat pp WHERE pp.platId = :platId " +
           "AND pp.isActive = true AND pp.reductionEnds > :now")
    boolean hasActivePromotion(@Param("platId") UUID platId, @Param("now") LocalDateTime now);

    /**
     * Deactivate expired promotions (batch update)
     * 
     * @param now current date and time
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE PromotionPlat pp SET pp.isActive = false, pp.updatedAt = :now " +
           "WHERE pp.isActive = true AND pp.reductionEnds <= :now")
    int deactivateExpiredPromotions(@Param("now") LocalDateTime now);

    /**
     * Deactivate all promotions for a specific plat
     * 
     * @param platId the plat ID
     * @param now current date and time
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE PromotionPlat pp SET pp.isActive = false, pp.updatedAt = :now " +
           "WHERE pp.platId = :platId AND pp.isActive = true")
    int deactivatePromotionsForPlat(@Param("platId") UUID platId, @Param("now") LocalDateTime now);

    /**
     * Find promotions created within a date range
     * 
     * @param startDate start date
     * @param endDate end date
     * @return List of promotions created within the date range
     */
    @Query("SELECT pp FROM PromotionPlat pp WHERE pp.createdAt >= :startDate AND pp.createdAt <= :endDate " +
           "ORDER BY pp.createdAt DESC")
    List<PromotionPlat> findPromotionsCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Get statistics: count of active promotions
     * 
     * @param now current date and time
     * @return number of currently active promotions
     */
    @Query("SELECT COUNT(pp) FROM PromotionPlat pp WHERE pp.isActive = true AND pp.reductionEnds > :now")
    long countActivePromotions(@Param("now") LocalDateTime now);

    /**
     * Get statistics: count of expired promotions
     * 
     * @param now current date and time
     * @return number of expired promotions
     */
    @Query("SELECT COUNT(pp) FROM PromotionPlat pp WHERE pp.reductionEnds <= :now")
    long countExpiredPromotions(@Param("now") LocalDateTime now);

    /**
     * Find the most recent promotion for a plat (regardless of status)
     * 
     * @param platId the plat ID
     * @return Optional containing the most recent promotion
     */
    Optional<PromotionPlat> findFirstByPlatIdOrderByCreatedAtDesc(UUID platId);

    /**
     * Delete expired and inactive promotions (cleanup)
     * 
     * @param cutoffDate date before which expired promotions should be deleted
     */
    @Modifying
    @Query("DELETE FROM PromotionPlat pp WHERE pp.isActive = false AND pp.reductionEnds < :cutoffDate")
    void deleteOldExpiredPromotions(@Param("cutoffDate") LocalDateTime cutoffDate);
}