package com.dineo_backend.dineo.notifications.service;

import com.dineo_backend.dineo.plats.model.PromotionPlat;
import java.util.UUID;

/**
 * Service interface for sending promotion emails to users
 */
public interface PromotionEmailService {
    
    /**
     * Send promotion email to all users when a chef creates a promotion
     * @param promotionPlat the promotion that was created
     */
    void sendPromotionEmailToAllUsers(PromotionPlat promotionPlat);
    
    /**
     * Send promotion email to a specific user
     * @param userId the user to send the email to
     * @param promotionPlat the promotion details
     */
    void sendPromotionEmailToUser(UUID userId, PromotionPlat promotionPlat);
}
