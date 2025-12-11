package com.dineo_backend.dineo.notifications.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.notifications.service.PromotionEmailService;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of PromotionEmailService
 * Sends promotional emails to users when chefs create promotions
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * MULTI-THREADING FLOW EXPLANATION:
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * THREAD 1 (HTTP Request Thread - "http-nio-8080-exec-1"):
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 1. Chef clicks "Create Promotion" in app                    │
 * │ 2. POST request arrives at PromotionPlatController          │
 * │ 3. PromotionPlatServiceImpl.createPromotion() is called     │
 * │ 4. Promotion saved to database                              │
 * │ 5. promotionEmailService.sendPromotionEmailToAllUsers()     │
 * │    ↓ Spring sees @Async and hands off to THREAD 2          │
 * │ 6. HTTP thread continues immediately (doesn't wait!)        │
 * │ 7. Returns "Success!" to chef's phone in ~100ms ✓          │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * THREAD 2 (Worker Thread - "task-1" from thread pool):
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 1. Receives task from HTTP thread                           │
 * │ 2. Fetches all users from database                          │
 * │ 3. Loop through users (one by one)                          │
 * │    - Send email to user 1 (300ms)                           │
 * │    - Send email to user 2 (300ms)                           │
 * │    - Send email to user 3 (300ms)                           │
 * │    ... continues for all users ...                          │
 * │ 4. Finishes after 30 seconds (100 users × 300ms)           │
 * │ 5. Thread returns to pool, ready for next task             │
 * └─────────────────────────────────────────────────────────────┘
 * 
 * KEY POINT: Chef gets "Success!" after 100ms, while emails are still 
 * being sent in the background. Chef doesn't wait!
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class PromotionEmailServiceImpl implements PromotionEmailService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionEmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:https://miamlo.app}")
    private String appUrl;

    @Override
    @Async  // ← THIS IS THE MAGIC! This tells Spring: "Run this method in a SEPARATE THREAD"
    public void sendPromotionEmailToAllUsers(PromotionPlat promotionPlat) {
        // ============ YOU ARE NOW IN A WORKER THREAD (NOT THE HTTP REQUEST THREAD) ============
        // Thread name: "task-1", "task-2", etc. (from Spring's thread pool)
        // The HTTP thread that called this method already returned to the chef!
        
        logger.info("Sending promotion emails to all users for promotion: {}", promotionPlat.getId());
        // ↑ This log appears AFTER the API already responded to chef

        // Get all users (no role filter)
        List<User> users = userRepository.findAll();
        // ↑ This database query happens in the WORKER THREAD, not HTTP thread

        logger.info("Found {} users to send promotion email", users.size());

        // ↓ This loop runs in the WORKER THREAD
        // It sends emails one-by-one (sequential), but chef doesn't wait for it
        for (User user : users) {
            try {
                sendPromotionEmailToUser(user.getId(), promotionPlat);
                // ↑ Each email takes ~300ms, but chef already got "Success!" response
            } catch (Exception e) {
                logger.error("Failed to send promotion email to user {}: {}", user.getId(), e.getMessage());
            }
        }

        logger.info("Finished sending promotion emails");
        // ↑ This appears 30 seconds later (after all emails sent)
        // Chef's phone showed "Success!" 30 seconds ago and they moved on
        
        // ============ WORKER THREAD FINISHES AND RETURNS TO THREAD POOL ============
    }

    @Override
    @Async
    public void sendPromotionEmailToUser(UUID userId, PromotionPlat promotionPlat) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.warn("User not found: {}", userId);
                return;
            }

            User user = userOpt.get();
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                logger.warn("User {} has no email address", userId);
                return;
            }

            // Get plat details
            Optional<Plat> platOpt = platRepository.findById(promotionPlat.getPlatId());
            if (platOpt.isEmpty()) {
                logger.warn("Plat not found: {}", promotionPlat.getPlatId());
                return;
            }
            Plat plat = platOpt.get();

            // Get chef details
            Optional<User> chefUserOpt = userRepository.findById(plat.getChefId());
            String chefName = chefUserOpt.map(chef -> chef.getFirstName() + " " + chef.getLastName())
                    .orElse("Chef");

            // Get chef cover image
            String chefCoverImg = null;
            Optional<ChefDescription> chefDescOpt = chefDescriptionRepository.findByUserId(plat.getChefId());
            if (chefDescOpt.isPresent()) {
                chefCoverImg = chefDescOpt.get().getChefCoverImg();
            }

            // Calculate prices
            double originalPrice = plat.getPrice();
            double discountPercentage = promotionPlat.getReductionValue().doubleValue(); // This is already a percentage
            double discountAmount = originalPrice * (discountPercentage / 100);
            double discountedPrice = originalPrice - discountAmount;
            int discountPercentageInt = (int) Math.round(discountPercentage);

            // Format expiration date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String expirationDate = promotionPlat.getReductionEnds().format(formatter);

            // Prepare email context
            Context context = new Context();
            context.setVariable("userName", user.getFirstName());
            context.setVariable("platName", plat.getName());
            context.setVariable("platDescription", plat.getDescription());
            context.setVariable("platImageUrl", plat.getImageUrl() != null ? plat.getImageUrl() : "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600");
            context.setVariable("chefName", chefName);
            context.setVariable("chefCoverImg", chefCoverImg);
            context.setVariable("originalPrice", String.format("%.2f€", originalPrice));
            context.setVariable("discountedPrice", String.format("%.2f€", discountedPrice));
            context.setVariable("discountPercentage", discountPercentageInt);
            context.setVariable("expirationDate", expirationDate);
            context.setVariable("appLink", appUrl + "/meal-details?platId=" + plat.getId());
            context.setVariable("unsubscribeLink", appUrl + "/unsubscribe?userId=" + user.getId());

            // Generate HTML content
            String htmlContent = templateEngine.process("promotion-email", context);

            // Send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🎉 Nouvelle Promotion sur " + plat.getName() + " - Miamlo");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Promotion email sent successfully to user: {} ({})", user.getEmail(), userId);

        } catch (MessagingException e) {
            logger.error("Failed to send promotion email to user {}: {}", userId, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending promotion email to user {}: {}", userId, e.getMessage(), e);
        }
    }
}
