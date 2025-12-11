package com.dineo_backend.dineo.notifications.batch;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.plats.model.PromotionPlat;

/**
 * Data Transfer Object for Batch Email Processing
 * 
 * This class holds all the data needed to send one promotional email
 * Spring Batch passes this between Processor and Writer
 */
public class PromotionEmailData {
    
    private User user;
    private PromotionPlat promotionPlat;

    public PromotionEmailData() {
    }

    public PromotionEmailData(User user, PromotionPlat promotionPlat) {
        this.user = user;
        this.promotionPlat = promotionPlat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PromotionPlat getPromotionPlat() {
        return promotionPlat;
    }

    public void setPromotionPlat(PromotionPlat promotionPlat) {
        this.promotionPlat = promotionPlat;
    }

    @Override
    public String toString() {
        return "PromotionEmailData{" +
                "userId=" + (user != null ? user.getId() : null) +
                ", email=" + (user != null ? user.getEmail() : null) +
                ", promotionId=" + (promotionPlat != null ? promotionPlat.getId() : null) +
                '}';
    }
}
