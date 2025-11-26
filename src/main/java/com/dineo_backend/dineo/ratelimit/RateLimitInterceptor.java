package com.dineo_backend.dineo.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to check rate limits before processing requests
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String key = getClientIdentifier(request);
        String path = request.getRequestURI();
        
        Bucket bucket;
        
        // Apply different rate limits based on endpoint
        if (isAuthEndpoint(path)) {
            bucket = rateLimitService.resolveAuthBucket(key);
        } else if (isOrderEndpoint(path)) {
            bucket = rateLimitService.resolveOrderBucket(key);
        } else if (isUploadEndpoint(path)) {
            bucket = rateLimitService.resolveUploadBucket(key);
        } else {
            bucket = rateLimitService.resolveBucket(key);
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed - add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\": \"Trop de requêtes. Veuillez réessayer dans %d secondes.\", \"status\": 429}",
                waitForRefill
            ));
            return false;
        }
    }

    /**
     * Get client identifier (IP address or user ID if authenticated)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get user ID from SecurityContext (set by JWT filter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && authentication.getPrincipal() instanceof String) {
            String userId = (String) authentication.getPrincipal();
            return "user:" + userId;
        }
        
        // Fall back to IP address
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For (take the first one)
        if (clientIp != null && clientIp.contains(",")) {
            clientIp = clientIp.split(",")[0].trim();
        }
        
        return "ip:" + clientIp;
    }

    /**
     * Check if endpoint is an authentication endpoint
     */
    private boolean isAuthEndpoint(String path) {
        return path.contains("/auth/login") 
            || path.contains("/auth/register")
            || path.contains("/auth/forgot-password")
            || path.contains("/auth/reset-password");
    }

    /**
     * Check if endpoint is an order endpoint
     */
    private boolean isOrderEndpoint(String path) {
        return path.contains("/orders") && !path.contains("/orders/");
    }

    /**
     * Check if endpoint is a file upload endpoint
     */
    private boolean isUploadEndpoint(String path) {
        return path.contains("/upload") 
            || path.contains("/plats") && path.contains("/image");
    }
}
