package com.dineo_backend.dineo.plats.scheduler;

import com.dineo_backend.dineo.plats.service.PromotionPlatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to automatically deactivate expired promotions
 * Runs every hour to check and deactivate expired promotions
 */
@Component
public class PromotionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PromotionScheduler.class);

    @Autowired
    private PromotionPlatService promotionService;

    /**
     * Scheduled task that runs once daily at midnight to deactivate expired promotions
     * Cron expression: "0 0 0 * * *" means:
     * - Second: 0
     * - Minute: 0
     * - Hour: 0 (midnight)
     * - Day of month: * (every day)
     * - Month: * (every month)
     * - Day of week: * (every day of week)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deactivateExpiredPromotions() {
        logger.info("Starting scheduled task: Deactivate expired promotions");
        
        try {
            int deactivatedCount = promotionService.deactivateExpiredPromotions();
            
            if (deactivatedCount > 0) {
                logger.info("Successfully deactivated {} expired promotion(s)", deactivatedCount);
            } else {
                logger.info("No expired promotions to deactivate");
            }
            
        } catch (Exception e) {
            logger.error("Error during scheduled promotion deactivation: {}", e.getMessage(), e);
        }
        
        logger.info("Completed scheduled task: Deactivate expired promotions");
    }
}
