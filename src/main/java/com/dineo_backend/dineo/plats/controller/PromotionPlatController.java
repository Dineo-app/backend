package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.dineo_backend.dineo.plats.dto.CreatePromotionRequest;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;
import com.dineo_backend.dineo.plats.service.PromotionPlatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for managing plat promotions
 */
@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionPlatController {

    private static final Logger logger = LoggerFactory.getLogger(PromotionPlatController.class);

    @Autowired
    private PromotionPlatService promotionService;

    /**
     * Create a new promotion for a plat
     * Only accessible by PROVIDER role (chefs)
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        try {
            // Get authenticated chef user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID chefUserId = UUID.fromString(authentication.getName());

            logger.info("Create promotion request received from chef user ID: {} for plat ID: {}", 
                    chefUserId, request.getPlatId());

            PromotionResponse promotion = promotionService.createPromotion(chefUserId, request);

            ApiResponse<PromotionResponse> apiResponse = ApiResponse.success(
                    "Promotion créée avec succès",
                    promotion
            );

            logger.info("Promotion created successfully with ID: {}", promotion.getId());
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error creating promotion: {}", e.getMessage());
            ApiResponse<PromotionResponse> errorResponse = ApiResponse.error(
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error creating promotion: {}", e.getMessage(), e);
            ApiResponse<PromotionResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la création de la promotion"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get active promotion for a specific plat
     * Accessible by all authenticated users
     */
    @GetMapping("/plat/{platId}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getActivePromotionByPlatId(
            @PathVariable UUID platId) {
        try {
            logger.info("Get active promotion request for plat ID: {}", platId);

            PromotionResponse promotion = promotionService.getActivePromotionByPlatId(platId);

            if (promotion == null) {
                ApiResponse<PromotionResponse> apiResponse = ApiResponse.success(
                        "Aucune promotion active pour ce plat",
                        null
                );
                return ResponseEntity.ok(apiResponse);
            }

            ApiResponse<PromotionResponse> apiResponse = ApiResponse.success(
                    "Promotion récupérée avec succès",
                    promotion
            );

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Error getting promotion for plat {}: {}", platId, e.getMessage(), e);
            ApiResponse<PromotionResponse> errorResponse = ApiResponse.error(
                    "Erreur lors de la récupération de la promotion"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get all promotions for the authenticated chef's plats
     * Only accessible by PROVIDER role (chefs)
     */
    @GetMapping("/my-promotions")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getChefPromotions() {
        try {
            // Get authenticated chef user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID chefUserId = UUID.fromString(authentication.getName());

            logger.info("Get promotions request from chef user ID: {}", chefUserId);

            List<PromotionResponse> promotions = promotionService.getChefPromotions(chefUserId);

            ApiResponse<List<PromotionResponse>> apiResponse = ApiResponse.success(
                    "Promotions récupérées avec succès",
                    promotions
            );

            logger.info("Retrieved {} promotions for chef user ID: {}", promotions.size(), chefUserId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Error getting chef promotions: {}", e.getMessage(), e);
            ApiResponse<List<PromotionResponse>> errorResponse = ApiResponse.error(
                    "Erreur lors de la récupération des promotions"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Delete a promotion manually
     * Only accessible by PROVIDER role (chefs)
     * Chef can only delete their own promotions
     */
    @DeleteMapping("/{promotionId}")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(@PathVariable UUID promotionId) {
        try {
            // Get authenticated chef user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID chefUserId = UUID.fromString(authentication.getName());

            logger.info("Delete promotion request for ID: {} from chef user ID: {}", 
                    promotionId, chefUserId);

            promotionService.deletePromotion(chefUserId, promotionId);

            ApiResponse<Void> apiResponse = ApiResponse.success(
                    "Promotion supprimée avec succès",
                    null
            );

            logger.info("Promotion deleted successfully with ID: {}", promotionId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error deleting promotion {}: {}", promotionId, e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error deleting promotion {}: {}", promotionId, e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la suppression de la promotion"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
