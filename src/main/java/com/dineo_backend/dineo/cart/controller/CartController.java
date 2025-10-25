package com.dineo_backend.dineo.cart.controller;

import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.cart.dto.*;
import com.dineo_backend.dineo.cart.service.CartService;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for cart management operations
 * Handles HTTP requests for cart-related endpoints
 */
@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtService jwtService;

    /**
     * Get user's cart with all items
     * @param authHeader the authorization header containing JWT token
     * @return the cart summary
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartSummaryResponse>> getUserCart(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Getting user cart");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            CartSummaryResponse cart = cartService.getUserCart(userId);

            return ResponseEntity.ok(ApiResponse.success("Panier récupéré avec succès", cart));

        } catch (Exception e) {
            logger.error("Error getting cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur lors de la récupération du panier"));
        }
    }

    /**
     * Add item to cart
     * @param request the add to cart request
     * @param authHeader the authorization header containing JWT token
     * @return the added cart item
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Adding item to cart: {}", request.getPlatId());

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            CartItemResponse cartItem = cartService.addToCart(request, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Article ajouté au panier avec succès", cartItem));

        } catch (Exception e) {
            logger.error("Error adding to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur lors de l'ajout au panier"));
        }
    }

    /**
     * Update cart item quantity
     * @param cartItemId the cart item ID
     * @param request the update request
     * @param authHeader the authorization header containing JWT token
     * @return the updated cart item
     */
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Updating cart item: {}", cartItemId);

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            CartItemResponse cartItem = cartService.updateCartItem(cartItemId, request, userId);

            return ResponseEntity.ok(ApiResponse.success("Article mis à jour avec succès", cartItem));

        } catch (RuntimeException e) {
            logger.error("Error updating cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur lors de la mise à jour"));
        }
    }

    /**
     * Remove item from cart
     * @param cartItemId the cart item ID
     * @param authHeader the authorization header containing JWT token
     * @return success response
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable UUID cartItemId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Removing cart item: {}", cartItemId);

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            cartService.removeFromCart(cartItemId, userId);

            return ResponseEntity.ok(ApiResponse.success("Article supprimé du panier", null));

        } catch (RuntimeException e) {
            logger.error("Error removing cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing cart item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur lors de la suppression"));
        }
    }

    /**
     * Clear entire cart
     * @param authHeader the authorization header containing JWT token
     * @return success response
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Clearing cart");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            cartService.clearCart(userId);

            return ResponseEntity.ok(ApiResponse.success("Panier vidé avec succès", null));

        } catch (Exception e) {
            logger.error("Error clearing cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur lors du vidage du panier"));
        }
    }

    /**
     * Get cart item count
     * @param authHeader the authorization header containing JWT token
     * @return the cart count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getCartCount(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Getting cart count");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            long count = cartService.getCartCount(userId);

            return ResponseEntity.ok(ApiResponse.success("Nombre d'articles récupéré", count));

        } catch (Exception e) {
            logger.error("Error getting cart count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erreur lors de la récupération du nombre d'articles"));
        }
    }
}
