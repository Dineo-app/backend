package com.dineo_backend.dineo.notifications.service.impl;

import com.dineo_backend.dineo.authentication.model.UserPushToken;
import com.dineo_backend.dineo.authentication.repository.UserPushTokenRepository;
import com.dineo_backend.dineo.notifications.service.ExpoPushService;
import com.dineo_backend.dineo.notifications.service.PromotionPushService;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of PromotionPushService that sends push notifications to all users
 * when a promotion is created. This runs asynchronously to not block the promotion creation.
 * 
 * FLOW EXPLANATION:
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │ 1. Chef creates promotion via mobile app                            │
 * │ 2. PromotionPlatServiceImpl.createPromotion() is called             │
 * │ 3. This method is triggered with @Async                             │
 * │ 4. Runs in SEPARATE THREAD - doesn't block HTTP response            │
 * │ 5. Fetches ALL push tokens from database                            │
 * │ 6. Sends notification to EVERY registered device                    │
 * │ 7. Each notification contains platId for deep linking               │
 * └─────────────────────────────────────────────────────────────────────┘
 */
@Service
public class PromotionPushServiceImpl implements PromotionPushService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionPushServiceImpl.class);

    @Autowired
    private UserPushTokenRepository pushTokenRepository;

    @Autowired
    private ExpoPushService expoPushService;

    @Autowired
    private PlatRepository platRepository;

    /**
     * Send promotion push notification to all users asynchronously.
     * This method runs in a separate thread pool managed by Spring's @Async.
     * 
     * @param promotionPlat the promotion that was created
     */
    @Override
    @Async("taskExecutor") // Uses thread pool for async execution
    public void sendPromotionPushToAllUsers(PromotionPlat promotionPlat) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.info("🔔 [ASYNC] Starting to send promotion push notifications for promotion ID: {}", 
                promotionPlat.getId());

            // Fetch the plat details
            Plat plat = platRepository.findById(promotionPlat.getPlatId())
                    .orElseThrow(() -> new RuntimeException("Plat not found"));

            // Calculate percentage discount
            BigDecimal priceDecimal = BigDecimal.valueOf(plat.getPrice());
            BigDecimal percentageDiscount = promotionPlat.getReductionValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(priceDecimal, 0, BigDecimal.ROUND_HALF_UP);

            // Create French notification message
            String title = String.format("🔥 Promo %d%% - %s", percentageDiscount.intValue(), plat.getName());
            String body = String.format("Économisez %.2f€ sur ce plat ! Offre limitée.", 
                promotionPlat.getReductionValue());

            // Prepare notification data for deep linking
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "PROMOTION");
            notificationData.put("platId", plat.getId().toString());
            notificationData.put("promotionId", promotionPlat.getId().toString());
            notificationData.put("reductionValue", promotionPlat.getReductionValue().toString());
            notificationData.put("percentageDiscount", percentageDiscount.intValue());
            notificationData.put("platName", plat.getName());

            // Get ALL push tokens (both authenticated and unauthenticated users)
            List<UserPushToken> allPushTokens = pushTokenRepository.findAll();
            
            if (allPushTokens.isEmpty()) {
                logger.warn("⚠️ No push tokens found in database. No notifications sent.");
                return;
            }

            logger.info("📱 Found {} push tokens. Sending notifications...", allPushTokens.size());

            int successCount = 0;
            int failureCount = 0;

            // Send notification to each device
            for (UserPushToken pushTokenEntry : allPushTokens) {
                try {
                    boolean sent = expoPushService.sendPushNotification(
                        pushTokenEntry.getPushToken(),
                        title,
                        body,
                        notificationData
                    );

                    if (sent) {
                        successCount++;
                        logger.debug("✅ Push sent to token: {}...", 
                            pushTokenEntry.getPushToken().substring(0, 20));
                    } else {
                        failureCount++;
                        logger.warn("❌ Failed to send push to token: {}...", 
                            pushTokenEntry.getPushToken().substring(0, 20));
                    }

                } catch (Exception e) {
                    failureCount++;
                    logger.error("❌ Error sending push to token {}: {}", 
                        pushTokenEntry.getPushToken(), e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ [ASYNC] Promotion push notifications completed in {}ms. Success: {}, Failed: {}", 
                duration, successCount, failureCount);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [ASYNC] Failed to send promotion push notifications after {}ms: {}", 
                duration, e.getMessage(), e);
        }
    }
}
