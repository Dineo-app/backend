package com.dineo_backend.dineo.cart.service;

import com.dineo_backend.dineo.cart.dto.AddToCartRequest;
import com.dineo_backend.dineo.cart.dto.CartItemResponse;
import com.dineo_backend.dineo.cart.dto.CartSummaryResponse;
import com.dineo_backend.dineo.cart.dto.UpdateCartItemRequest;

import java.util.UUID;

/**
 * Service interface for cart operations
 * Handles cart item management and business logic
 */
public interface CartService {

    /**
     * Get all cart items for a user
     * @param userId the user's ID
     * @return cart summary with all items and totals
     */
    CartSummaryResponse getUserCart(UUID userId);

    /**
     * Add an item to cart
     * If item already exists, quantity is increased
     * @param request the add to cart request
     * @param userId the user's ID
     * @return the added/updated cart item
     */
    CartItemResponse addToCart(AddToCartRequest request, UUID userId);

    /**
     * Update cart item quantity
     * @param cartItemId the cart item ID
     * @param request the update request
     * @param userId the user's ID
     * @return the updated cart item
     */
    CartItemResponse updateCartItem(UUID cartItemId, UpdateCartItemRequest request, UUID userId);

    /**
     * Remove an item from cart
     * @param cartItemId the cart item ID
     * @param userId the user's ID
     */
    void removeFromCart(UUID cartItemId, UUID userId);

    /**
     * Clear all items from user's cart
     * @param userId the user's ID
     */
    void clearCart(UUID userId);

    /**
     * Get cart item count for a user
     * @param userId the user's ID
     * @return total number of items in cart
     */
    long getCartCount(UUID userId);
}
