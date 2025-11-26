package com.dineo_backend.dineo.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle rate limiting using token bucket algorithm
 */
@Service
public class RateLimitService {

    // Cache to store buckets per IP/User
    private final Cache<String, Bucket> cache;

    public RateLimitService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(100_000)
                .build();
    }

    /**
     * Get or create a bucket for general API requests
     * Limit: 100 requests per minute
     */
    public Bucket resolveBucket(String key) {
        return cache.get(key, k -> createGeneralBucket());
    }

    /**
     * Get or create a bucket for authentication endpoints (login, register)
     * Limit: 10 requests per minute (stricter for security)
     */
    public Bucket resolveAuthBucket(String key) {
        return cache.get("auth:" + key, k -> createAuthBucket());
    }

    /**
     * Get or create a bucket for order creation
     * Limit: 20 requests per minute
     */
    public Bucket resolveOrderBucket(String key) {
        return cache.get("order:" + key, k -> createOrderBucket());
    }

    /**
     * Get or create a bucket for file uploads
     * Limit: 10 requests per minute
     */
    public Bucket resolveUploadBucket(String key) {
        return cache.get("upload:" + key, k -> createUploadBucket());
    }

    /**
     * General API rate limit: 100 requests per minute
     */
    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Auth endpoints rate limit: 10 requests per minute
     * Prevents brute force attacks
     */
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Order creation rate limit: 20 requests per minute
     * Prevents spam orders
     */
    private Bucket createOrderBucket() {
        Bandwidth limit = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * File upload rate limit: 10 requests per minute
     * Prevents storage abuse
     */
    private Bucket createUploadBucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Clear cache for testing or admin purposes
     */
    public void clearCache() {
        cache.invalidateAll();
    }

    /**
     * Remove a specific key from cache
     */
    public void removeKey(String key) {
        cache.invalidate(key);
    }
}
