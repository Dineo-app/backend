package com.dineo_backend.dineo.notifications.batch;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Spring Batch ItemWriter for Sending Promotional Emails
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * HOW THE WRITER WORKS:
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * The write() method receives a CHUNK of items (List of 50 users)
 * 
 * Example Flow:
 * 1. Thread "email-batch-1" calls write() with users 1-50
 * 2. Thread "email-batch-2" calls write() with users 51-100
 * 3. Thread "email-batch-3" calls write() with users 101-150
 * 4. Thread "email-batch-4" calls write() with users 151-200
 * 
 * All 4 threads run SIMULTANEOUSLY! Each processes its own chunk.
 * 
 * Within each chunk, we loop through users sequentially,
 * but different chunks are processed in parallel.
 * 
 * Result: 4x faster than single-threaded processing!
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
public class PromotionEmailWriter implements ItemWriter<PromotionEmailData> {

    private static final Logger logger = LoggerFactory.getLogger(PromotionEmailWriter.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:https://miamlo.app}")
    private String appUrl;

    /**
     * Write (send) emails for a chunk of users
     * 
     * @param chunk List of 50 users to send emails to
     * 
     * This method is called by MULTIPLE THREADS in parallel:
     * - Thread 1 processes chunk 1 (users 1-50)
     * - Thread 2 processes chunk 2 (users 51-100)
     * - Thread 3 processes chunk 3 (users 101-150)
     * - Thread 4 processes chunk 4 (users 151-200)
     * 
     * All happening at the SAME TIME!
     */
    @Override
    public void write(Chunk<? extends PromotionEmailData> chunk) throws Exception {
        String threadName = Thread.currentThread().getName();
        logger.info("[{}] Processing chunk of {} users", threadName, chunk.size());

        int successCount = 0;
        int failCount = 0;

        // Loop through each user in this chunk
        for (PromotionEmailData emailData : chunk) {
            try {
                sendEmail(emailData);
                successCount++;
            } catch (Exception e) {
                failCount++;
                logger.error("[{}] Failed to send email to user {}: {}", 
                    threadName, emailData.getUser().getId(), e.getMessage());
                // Continue with next user even if this one fails
            }
        }

        logger.info("[{}] Chunk complete: {} sent, {} failed", 
            threadName, successCount, failCount);
    }

    /**
     * Send a single promotional email
     * (Same logic as before, but now runs in parallel chunks)
     */
    private void sendEmail(PromotionEmailData emailData) throws MessagingException {
        User user = emailData.getUser();
        var promotionPlat = emailData.getPromotionPlat();

        if (promotionPlat == null) {
            logger.warn("No promotion data for user {}", user.getId());
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
        double discountPercentage = promotionPlat.getReductionValue().doubleValue();
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
        logger.debug("Promotion email sent to: {}", user.getEmail());
    }
}
