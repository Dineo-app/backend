package com.dineo_backend.dineo.notifications.service;

import com.dineo_backend.dineo.plats.model.PromotionPlat;

/**
 * Service interface for sending promotion push notifications to all users
 */
public interface PromotionPushService {
    
    /**
     * Send push notification to all users when a chef creates a promotion.
     * The notification will be sent to ALL registered push tokens (authenticated or not).
     * 
     * @param promotionPlat the promotion that was created
     */
    void sendPromotionPushToAllUsers(PromotionPlat promotionPlat);
}
