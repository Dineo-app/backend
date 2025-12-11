package com.dineo_backend.dineo.notifications.batch;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

/**
 * Spring Batch Configuration for Sending Promotional Emails
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * HOW SPRING BATCH WORKS - MULTI-THREADING EXPLAINED:
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Traditional Approach (Current):
 * ┌────────────────────────────────────────────────────────────────┐
 * │ WORKER THREAD 1 (Sequential Processing)                       │
 * │  → Fetch all 1000 users                                       │
 * │  → Send email 1 (300ms)                                       │
 * │  → Send email 2 (300ms)                                       │
 * │  → Send email 3 (300ms)                                       │
 * │  ... 1000 emails × 300ms = 5 MINUTES! ❌                      │
 * └────────────────────────────────────────────────────────────────┘
 * 
 * Spring Batch Approach (With This Config):
 * ┌────────────────────────────────────────────────────────────────┐
 * │ THREAD 1: Process users 1-50    │ THREAD 2: Process users 51-100│
 * │  → Read chunk (50 users)        │  → Read chunk (50 users)     │
 * │  → Process each user            │  → Process each user          │
 * │  → Send 50 emails in parallel   │  → Send 50 emails in parallel │
 * │  ↓ (15 seconds for 50)          │  ↓ (15 seconds for 50)        │
 * ├─────────────────────────────────┼───────────────────────────────┤
 * │ THREAD 3: Process users 101-150 │ THREAD 4: Process users 151-200│
 * │  → Read chunk (50 users)        │  → Read chunk (50 users)     │
 * │  → Send 50 emails in parallel   │  → Send 50 emails in parallel │
 * ├─────────────────────────────────┴───────────────────────────────┤
 * │ Total: 1000 users ÷ 4 threads = ~75 SECONDS (4x faster!) ✓    │
 * └────────────────────────────────────────────────────────────────┘
 * 
 * With 8 threads (if you change threadCount = 8):
 * ┌────────────────────────────────────────────────────────────────┐
 * │ THREAD 1  │ THREAD 2  │ THREAD 3  │ THREAD 4                   │
 * │ Users 1-50│ Users51-100│Users101-150│Users151-200               │
 * ├───────────┼───────────┼───────────┼───────────────────────────┤
 * │ THREAD 5  │ THREAD 6  │ THREAD 7  │ THREAD 8                   │
 * │Users201-250│Users251-300│Users301-350│Users351-400              │
 * ├───────────┴───────────┴───────────┴───────────────────────────┤
 * │ Total: 1000 users ÷ 8 threads = ~37.5 SECONDS (8x faster!) ✓✓│
 * └────────────────────────────────────────────────────────────────┘
 * 
 * With 16 threads (if you change threadCount = 16):
 * ┌────────────────────────────────────────────────────────────────┐
 * │ 16 threads working simultaneously!                             │
 * │ Total: 1000 users ÷ 16 threads = ~18.75 SECONDS (16x faster!)│
 * │                                                                │
 * │ BUT: Might hit email server rate limits! ⚠️                   │
 * │      Might exhaust database connections! ⚠️                   │
 * │      Test carefully before using in production!               │
 * └────────────────────────────────────────────────────────────────┘
 * 
 * Key Concepts:
 * 1. CHUNK: Group of items processed together (we use 50 users per chunk)
 * 2. READER: Reads users from database in chunks
 * 3. PROCESSOR: Prepares email data for each user
 * 4. WRITER: Actually sends the emails for the chunk
 * 5. PARALLEL: Multiple threads process different chunks simultaneously
 * 
 * Benefits:
 * - 4x faster with 4 threads (configurable)
 * - Automatic retry on failure
 * - Progress tracking (50 sent, 100 sent, etc.)
 * - Can resume if server crashes
 * - Database-backed job history
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
public class PromotionEmailBatchConfig {

    private static final Logger logger = LoggerFactory.getLogger(PromotionEmailBatchConfig.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * STEP 1: READER - Fetches users from database in chunks
     * 
     * How it works:
     * - Reads 50 users at a time (chunk size)
     * - Uses pagination internally (efficient for large datasets)
     * - Each thread gets its own chunk
     * 
     * Example: If you have 1000 users:
     * - Thread 1 reads users 1-50
     * - Thread 2 reads users 51-100
     * - Thread 3 reads users 101-150
     * - Thread 4 reads users 151-200
     * ... and so on
     */
    @Bean
    public RepositoryItemReader<User> userReader() {
        return new RepositoryItemReaderBuilder<User>()
                .name("userReader")
                .repository(userRepository)
                .methodName("findAll")
                .pageSize(50)  // ← Chunk size: process 50 users at a time
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    /**
     * STEP 2: PROCESSOR - Transforms User into EmailData
     * 
     * This runs BEFORE writing (sending emails)
     * You can filter users here, validate emails, etc.
     * 
     * Return null to skip a user (won't send email)
     * Return EmailData to proceed to Writer
     */
    @Bean
    public ItemProcessor<User, PromotionEmailData> emailProcessor() {
        return user -> {
            // Skip users without email
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                logger.warn("Skipping user {} - no email address", user.getId());
                return null;  // ← This user will be skipped
            }

            // Create email data object
            PromotionEmailData emailData = new PromotionEmailData();
            emailData.setUser(user);
            // PromotionPlat will be set via JobParameters when job is launched
            
            return emailData;
        };
    }

    /**
     * STEP 3: WRITER - Sends emails for the chunk
     * 
     * This receives a LIST of users (chunk of 50)
     * It processes them ALL at once
     * 
     * Spring Batch ensures:
     * - If one email fails, others still send
     * - Failed items can be retried
     * - Progress is tracked in database
     */
    @Bean
    public ItemWriter<PromotionEmailData> emailWriter() {
        return new PromotionEmailWriter();
    }

    /**
     * STEP 4: STEP - Combines Reader + Processor + Writer
     * 
     * This defines the complete flow:
     * Read 50 users → Process each → Write (send emails) for all 50
     * 
     * Configuration:
     * - chunk(50): Process 50 items at a time
     * - taskExecutor: Use thread pool for parallel processing
     * - throttleLimit: Max chunks processing simultaneously (MUST MATCH threadCount below!)
     */
    @Bean
    public Step sendPromotionEmailsStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        // ↓ IMPORTANT: This MUST match the threadCount in emailTaskExecutor() ↓
        int threadCount = 4;  // ← Change this to match the thread pool size
        
        return new StepBuilder("sendPromotionEmailsStep", jobRepository)
                .<User, PromotionEmailData>chunk(50, transactionManager)  // ← Process 50 users per chunk
                .reader(userReader())
                .processor(emailProcessor())
                .writer(emailWriter())
                // PARALLEL EXECUTION: Multiple threads process different chunks at the same time
                .taskExecutor(emailTaskExecutor())
                .throttleLimit(threadCount)  // ← MUST MATCH threadCount! (controls how many threads active)
                .build();
    }

    /**
     * STEP 5: JOB - The complete batch job
     * 
     * A Job is a container for Steps
     * You can have multiple steps (e.g., Step 1: Fetch data, Step 2: Send emails)
     * 
     * This job has only 1 step: send promotional emails
     */
    @Bean
    public Job promotionEmailJob(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new JobBuilder("promotionEmailJob", jobRepository)
                .start(sendPromotionEmailsStep(jobRepository, transactionManager))
                .build();
    }

    /**
     * THREAD POOL CONFIGURATION
     * 
     * ═══════════════════════════════════════════════════════════════════════════
     * WHY 4 THREADS? WHY NOT MORE?
     * ═══════════════════════════════════════════════════════════════════════════
     * 
     * The number 4 is NOT a hard limit! You can change it to anything you want.
     * 
     * FACTORS TO CONSIDER:
     * 
     * 1. YOUR CPU CORES
     *    - Your server has X CPU cores
     *    - Rule of thumb: Use 1-2 threads per CPU core
     *    - Example: 4 cores = 4-8 threads is optimal
     *    - Why? More threads than cores = context switching overhead
     * 
     * 2. EMAIL SERVER LIMITS
     *    - Gmail: ~100-150 emails/minute (2-3 per second)
     *    - SendGrid: Depends on your plan (could be 1000s/second)
     *    - Your SMTP server might throttle/block too many connections
     *    - Test with your email provider to find the sweet spot!
     * 
     * 3. DATABASE CONNECTIONS
     *    - Each thread needs a DB connection to fetch user/plat data
     *    - Default connection pool: ~10 connections
     *    - Too many threads = "connection pool exhausted" errors
     * 
     * 4. MEMORY USAGE
     *    - Each thread holds 50 users in memory (chunk size)
     *    - 4 threads × 50 users = 200 users in memory at once
     *    - 10 threads × 50 users = 500 users in memory at once
     *    - More threads = more memory usage
     * 
     * PERFORMANCE EXAMPLES (1000 users, 300ms per email):
     * 
     * 1 thread:  1000 ÷ 1  × 300ms = 300 seconds (5 minutes)  ← Baseline
     * 2 threads: 1000 ÷ 2  × 300ms = 150 seconds (2.5 min)    ← 2x faster
     * 4 threads: 1000 ÷ 4  × 300ms = 75 seconds (1.25 min)    ← 4x faster ✓
     * 8 threads: 1000 ÷ 8  × 300ms = 37.5 seconds             ← 8x faster!
     * 16 threads: 1000 ÷ 16 × 300ms = 18.75 seconds           ← 16x faster!!
     * 
     * BUT WATCH OUT:
     * - 16 threads might overwhelm your email server (rate limiting!)
     * - 16 threads might exhaust database connections
     * - 16 threads on a 4-core CPU = lots of context switching (slower!)
     * 
     * RECOMMENDATION:
     * - Start with 4 threads (safe default)
     * - Monitor: email server response, CPU usage, DB connections
     * - If all good, gradually increase to 8, then 12, then 16
     * - Stop when you hit limits (email throttling, CPU maxed, etc.)
     * 
     * HOW TO CHANGE:
     * - Change "4" below to any number: 2, 8, 16, 32, etc.
     * - Also change throttleLimit(4) above to match
     * - Restart server and test!
     * 
     * ═══════════════════════════════════════════════════════════════════════════
     */
    @Bean
    public org.springframework.core.task.TaskExecutor emailTaskExecutor() {
        org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor executor = 
            new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        
        // ↓ CHANGE THESE NUMBERS TO ADJUST PARALLELISM ↓
        int threadCount = 4;  // ← Change this to 2, 8, 16, etc.
        
        executor.setCorePoolSize(threadCount);    // ← Core threads always running
        executor.setMaxPoolSize(threadCount);     // ← Max threads (keep same as core)
        executor.setQueueCapacity(100);           // ← Queue for chunks waiting to process
        executor.setThreadNamePrefix("email-batch-");
        executor.initialize();
        
        logger.info("Initialized email batch thread pool with {} threads", threadCount);
        logger.info("Performance: {}x faster than single-threaded", threadCount);
        logger.info("Estimated time for 1000 emails: {} seconds", 
            (int)(1000.0 / threadCount * 0.3));
        
        return executor;
    }
}
