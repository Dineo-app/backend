package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.model.UserPushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing user push tokens.
 * Supports multi-device push notifications.
 */
@Repository
public interface UserPushTokenRepository extends JpaRepository<UserPushToken, UUID> {
    
    /**
     * Find all push tokens for a user (all devices)
     */
    List<UserPushToken> findByUserId(UUID userId);
    
    /**
     * Find a specific push token
     */
    Optional<UserPushToken> findByPushToken(String pushToken);
    
    /**
     * Find by user and token (to check if already registered)
     */
    Optional<UserPushToken> findByUserIdAndPushToken(UUID userId, String pushToken);
    
    /**
     * Delete all tokens for a user (on account deletion)
     */
    void deleteByUserId(UUID userId);
    
    /**
     * Delete a specific token (on logout from one device)
     */
    void deleteByPushToken(String pushToken);
    
    /**
     * Delete token for a specific user and device
     */
    void deleteByUserIdAndPushToken(UUID userId, String pushToken);
    
    /**
     * Check if a token exists for a user
     */
    boolean existsByUserIdAndPushToken(UUID userId, String pushToken);
}
