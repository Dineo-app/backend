package com.dineo_backend.dineo.plats.service;

import com.dineo_backend.dineo.plats.dto.CreatePromotionRequest;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing plat promotions
 */
public interface PromotionPlatService {

    /**
     * Create a new promotion for a plat
     * Only the chef who owns the plat can create a promotion
     *
     * @param chefUserId the chef's user ID
     * @param request the promotion details
     * @return the created promotion
     */
    PromotionResponse createPromotion(UUID chefUserId, CreatePromotionRequest request);

    /**
     * Get active promotion for a specific plat
     *
     * @param platId the plat ID
     * @return the active promotion if exists, null otherwise
     */
    PromotionResponse getActivePromotionByPlatId(UUID platId);

    /**
     * Get all promotions for plats owned by a specific chef
     *
     * @param chefUserId the chef's user ID
     * @return list of promotions
     */
    List<PromotionResponse> getChefPromotions(UUID chefUserId);

    /**
     * Delete a promotion manually
     * Only the chef who owns the plat can delete the promotion
     *
     * @param chefUserId the chef's user ID
     * @param promotionId the promotion ID
     */
    void deletePromotion(UUID chefUserId, UUID promotionId);

    /**
     * Deactivate expired promotions (called by cron job)
     *
     * @return number of promotions deactivated
     */
    int deactivateExpiredPromotions();
}
