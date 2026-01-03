package com.dineo_backend.dineo.cart.service;

import com.dineo_backend.dineo.cart.dto.*;
import com.dineo_backend.dineo.cart.model.CartItem;
import com.dineo_backend.dineo.cart.model.CartItemIngredient;
import com.dineo_backend.dineo.cart.repository.CartItemRepository;
import com.dineo_backend.dineo.cart.repository.CartItemIngredientRepository;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.model.Ingredient;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PromotionPlatRepository;
import com.dineo_backend.dineo.plats.repository.IngredientRepository;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CartService
 * Handles cart operations and business logic
 */
@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartItemIngredientRepository cartItemIngredientRepository;

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private PromotionPlatRepository promotionPlatRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Override
    public CartSummaryResponse getUserCart(UUID userId) {
        logger.info("Getting cart for user: {}", userId);

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        // Calculate totals
        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        double subtotal = itemResponses.stream()
                .mapToDouble(CartItemResponse::getTotalPrice)
                .sum();

        return new CartSummaryResponse(itemResponses, totalItems, subtotal, subtotal);
    }

    @Override
    @Transactional
    public CartItemResponse addToCart(AddToCartRequest request, UUID userId) {
        logger.info("Adding plat {} to cart for user {}", request.getPlatId(), userId);
        
        // Check if the plat exists and get chef info
        Plat plat = platRepository.findById(request.getPlatId())
                .orElseThrow(() -> new RuntimeException("Plat non trouvé"));
        
        // Check if chef is open
        Optional<ChefDescription> chefDescOpt = chefDescriptionRepository.findByUserId(plat.getChefId());
        if (chefDescOpt.isPresent() && Boolean.FALSE.equals(chefDescOpt.get().getIsOpen())) {
            // Get chef name for error message
            Optional<User> chefOpt = userRepository.findById(plat.getChefId());
            String chefName = chefOpt.map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Ce chef");
            throw new RuntimeException("La cuisine de " + chefName + " est fermée. Impossible d'ajouter ce plat au panier.");
        }

        // Check if item already exists in cart
        CartItem cartItem = cartItemRepository.findByUserIdAndPlatId(userId, request.getPlatId())
                .orElse(null);

        if (cartItem != null) {
            // Update quantity if item exists
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            logger.info("Updated existing cart item quantity to: {}", cartItem.getQuantity());
            
            // Delete old ingredients and add new ones
            if (request.getSelectedIngredientIds() != null) {
                cartItemIngredientRepository.deleteByCartItemId(cartItem.getId());
            }
        } else {
            // Create new cart item
            cartItem = new CartItem(userId, request.getPlatId(), request.getQuantity());
            logger.info("Created new cart item");
        }

        cartItem = cartItemRepository.save(cartItem);
        
        // Save selected ingredients
        if (request.getSelectedIngredientIds() != null && !request.getSelectedIngredientIds().isEmpty()) {
            for (UUID ingredientId : request.getSelectedIngredientIds()) {
                Optional<Ingredient> ingredientOpt = ingredientRepository.findById(ingredientId);
                if (ingredientOpt.isPresent()) {
                    Ingredient ingredient = ingredientOpt.get();
                    CartItemIngredient cartItemIngredient = new CartItemIngredient(
                        cartItem.getId(),
                        ingredient.getId(),
                        ingredient.getName(),
                        ingredient.getPrice(),
                        ingredient.getIsFree()
                    );
                    cartItemIngredientRepository.save(cartItemIngredient);
                    logger.info("Added ingredient {} to cart item {}", ingredient.getName(), cartItem.getId());
                }
            }
        }
        
        return convertToCartItemResponse(cartItem);
    }

    @Override
    @Transactional
    public CartItemResponse updateCartItem(UUID cartItemId, UpdateCartItemRequest request, UUID userId) {
        logger.info("Updating cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Article de panier non trouvé"));

        // Verify the cart item belongs to the user
        if (!cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("Non autorisé à modifier cet article");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        return convertToCartItemResponse(cartItem);
    }

    @Override
    @Transactional
    public void removeFromCart(UUID cartItemId, UUID userId) {
        logger.info("Removing cart item {} for user {}", cartItemId, userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Article de panier non trouvé"));

        // Verify the cart item belongs to the user
        if (!cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("Non autorisé à supprimer cet article");
        }

        // Delete associated ingredients first
        cartItemIngredientRepository.deleteByCartItemId(cartItemId);
        
        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(UUID userId) {
        logger.info("Clearing cart for user: {}", userId);
        cartItemRepository.deleteByUserId(userId);
    }

    @Override
    public long getCartCount(UUID userId) {
        return cartItemRepository.countByUserId(userId);
    }

    /**
     * Convert CartItem entity to CartItemResponse DTO
     */
    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setPlatId(cartItem.getPlatId());
        response.setQuantity(cartItem.getQuantity());
        response.setAddedAt(cartItem.getAddedAt());
        response.setUpdatedAt(cartItem.getUpdatedAt());

        // Fetch plat details
        try {
            Optional<Plat> platOpt = platRepository.findById(cartItem.getPlatId());
            if (platOpt.isEmpty()) {
                response.setPlatName("Produit non disponible");
                response.setPlatPrice(0.0);
                response.setTotalPrice(0.0);
                return response;
            }

            Plat plat = platOpt.get();
            response.setPlatName(plat.getName());
            response.setPlatDescription(plat.getDescription());
            response.setPlatImageUrl(plat.getImageUrl());
            
            // Get active promotion if exists
            Optional<PromotionPlat> promotionOpt = promotionPlatRepository.findActivePromotionByPlatId(
                plat.getId(),
                LocalDateTime.now()
            );
            
            // Calculate price (with promotion if exists)
            double actualPrice = plat.getPrice();
            if (promotionOpt.isPresent()) {
                PromotionPlat promotion = promotionOpt.get();
                double discountPercentage = promotion.getReductionValue().doubleValue();
                double discountedPrice = plat.getPrice() * (1 - discountPercentage / 100.0);
                actualPrice = discountedPrice;
                
                // Set promotion info
                CartItemResponse.PromotionInfo promotionInfo = new CartItemResponse.PromotionInfo();
                promotionInfo.setId(promotion.getId());
                promotionInfo.setDiscountPercentage(discountPercentage);
                promotionInfo.setOriginalPrice(plat.getPrice());
                promotionInfo.setDiscountedPrice(discountedPrice);
                response.setPromotion(promotionInfo);
            }
            
            response.setPlatPrice(actualPrice);
            
            // Get selected ingredients for this cart item
            List<CartItemIngredient> cartItemIngredients = cartItemIngredientRepository.findByCartItemId(cartItem.getId());
            if (!cartItemIngredients.isEmpty()) {
                List<CartItemResponse.IngredientInfo> ingredientInfos = cartItemIngredients.stream()
                    .map(cii -> new CartItemResponse.IngredientInfo(
                        cii.getIngredientId(),
                        cii.getIngredientName(),
                        cii.getIngredientPrice(),
                        cii.getIsFree()
                    ))
                    .collect(Collectors.toList());
                response.setSelectedIngredients(ingredientInfos);
                
                // Add paid ingredients price to total
                double ingredientsPrice = cartItemIngredients.stream()
                    .filter(cii -> !cii.getIsFree())
                    .mapToDouble(CartItemIngredient::getIngredientPrice)
                    .sum();
                actualPrice += ingredientsPrice;
            }
            
            response.setTotalPrice(actualPrice * cartItem.getQuantity());
            
            // Set chef name and chef ID
            response.setChefId(plat.getChefId()); // Add chef ID for order creation
            Optional<User> chefOpt = userRepository.findById(plat.getChefId());
            if (chefOpt.isPresent()) {
                User chef = chefOpt.get();
                response.setChefName(chef.getFirstName() + " " + chef.getLastName());
            }
        } catch (Exception e) {
            logger.error("Error fetching plat details for cart item: {}", cartItem.getPlatId(), e);
            response.setPlatName("Produit non disponible");
            response.setPlatPrice(0.0);
            response.setTotalPrice(0.0);
        }

        return response;
    }
}
