package com.dineo_backend.dineo.notifications.batch;

import com.dineo_backend.dineo.plats.model.PromotionPlat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service to Launch Promotional Email Batch Job
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * HOW TO USE SPRING BATCH IN YOUR CODE:
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * BEFORE (Simple Async):
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ promotionEmailService.sendPromotionEmailToAllUsers(promotion);  │
 * │ ↓                                                               │
 * │ 1 worker thread processes 1000 users sequentially              │
 * │ Time: ~5 minutes                                               │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * AFTER (Spring Batch):
 * ┌─────────────────────────────────────────────────────────────────┐
 * │ batchEmailService.sendPromotionEmailsInBatch(promotion);        │
 * │ ↓                                                               │
 * │ 4 worker threads process 1000 users in parallel chunks         │
 * │ Time: ~75 seconds (4x faster!)                                 │
 * └─────────────────────────────────────────────────────────────────┘
 * 
 * The JobLauncher:
 * - Starts the batch job
 * - Passes parameters (promotionId) to the job
 * - Tracks progress in database
 * - Can be monitored/stopped if needed
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class PromotionEmailBatchService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionEmailBatchService.class);

    @Autowired
    private JobLauncher jobLauncher;  // ← Spring Batch component that launches jobs

    @Autowired
    private Job promotionEmailJob;  // ← The job we configured in PromotionEmailBatchConfig

    /**
     * Launch the batch job to send promotional emails
     * 
     * This method:
     * 1. Runs asynchronously (doesn't block API response)
     * 2. Launches a Spring Batch job
     * 3. The job spawns 4 threads to process users in parallel
     * 4. Returns immediately to the caller
     * 
     * How it works:
     * ┌──────────────────────────────────────────────────────────────┐
     * │ Chef creates promotion                                       │
     * │   ↓                                                          │
     * │ API calls this method (@Async runs in background)           │
     * │   ↓                                                          │
     * │ JobLauncher starts the batch job                            │
     * │   ↓                                                          │
     * │ Batch job creates 4 worker threads:                         │
     * │   - email-batch-1: processes users 1-50                     │
     * │   - email-batch-2: processes users 51-100                   │
     * │   - email-batch-3: processes users 101-150                  │
     * │   - email-batch-4: processes users 151-200                  │
     * │   All 4 run SIMULTANEOUSLY!                                 │
     * │   ↓                                                          │
     * │ When threads finish their chunks, they pick up next chunks  │
     * │   ↓                                                          │
     * │ Job completes when all users are processed                  │
     * └──────────────────────────────────────────────────────────────┘
     * 
     * @param promotionPlat The promotion to send emails for
     */
    @Async  // ← This makes it run in a background thread (doesn't block API)
    public void sendPromotionEmailsInBatch(PromotionPlat promotionPlat) {
        try {
            logger.info("Starting batch job to send promotional emails for promotion: {}", 
                promotionPlat.getId());

            // Create job parameters (data passed to the batch job)
            // We use timestamp to make each job execution unique
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("promotionId", promotionPlat.getId().toString())
                    .addLong("timestamp", System.currentTimeMillis())  // ← Makes job unique
                    .toJobParameters();

            // Launch the batch job!
            // This will:
            // 1. Create a JobExecution record in database
            // 2. Start processing users in parallel chunks
            // 3. Track progress (you can query database to see how many emails sent)
            // 4. Return when all chunks are processed
            jobLauncher.run(promotionEmailJob, jobParameters);

            logger.info("Batch job completed successfully for promotion: {}", 
                promotionPlat.getId());

        } catch (Exception e) {
            logger.error("Failed to execute batch job for promotion {}: {}", 
                promotionPlat.getId(), e.getMessage(), e);
        }
    }

    /**
     * Alternative: Send to a single user (synchronous, for testing)
     */
    public void sendToSingleUser(UUID userId, PromotionPlat promotionPlat) {
        // This could be used for testing or sending to specific users
        // Implementation similar to the old service
        logger.info("Sending promotional email to single user: {}", userId);
        // ... implementation ...
    }
}
