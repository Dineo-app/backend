package com.dineo_backend.dineo.cart.repository;

import com.dineo_backend.dineo.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for CartItem entity
 * Provides database access methods for cart operations
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    /**
     * Find all cart items for a specific user
     * @param userId the user's ID
     * @return list of cart items
     */
    List<CartItem> findByUserId(UUID userId);

    /**
     * Find a specific cart item by user and plat
     * @param userId the user's ID
     * @param platId the plat's ID
     * @return optional cart item
     */
    Optional<CartItem> findByUserIdAndPlatId(UUID userId, UUID platId);

    /**
     * Delete all cart items for a specific user
     * @param userId the user's ID
     */
    void deleteByUserId(UUID userId);

    /**
     * Count cart items for a specific user
     * @param userId the user's ID
     * @return count of cart items
     */
    long countByUserId(UUID userId);

    /**
     * Check if a cart item exists for user and plat
     * @param userId the user's ID
     * @param platId the plat's ID
     * @return true if exists
     */
    boolean existsByUserIdAndPlatId(UUID userId, UUID platId);
}
