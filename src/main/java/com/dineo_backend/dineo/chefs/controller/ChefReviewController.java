package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.chefs.dto.AddChefReviewRequest;
import com.dineo_backend.dineo.chefs.dto.ChefReviewResponse;
import com.dineo_backend.dineo.chefs.service.ChefReviewService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
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
 * REST Controller for chef review operations
 */
@RestController
@RequestMapping("/api/v1/chefs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChefReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChefReviewController.class);
    
    @Autowired
    private ChefReviewService chefReviewService;
    
    /**
     * Add a review for a chef
     * Only authenticated users can add reviews
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<ChefReviewResponse>> addReview(
            @Valid @RequestBody AddChefReviewRequest request) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Add chef review request received from user {} for chef {}", userId, request.getChefId());
            
            ChefReviewResponse response = chefReviewService.addReview(userId, request);
            
            ApiResponse<ChefReviewResponse> apiResponse = ApiResponse.success(
                    "Avis ajouté au chef avec succès",
                    response
            );
            
            logger.info("Chef review added successfully for chef {} by user {}", request.getChefId(), userId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (RuntimeException e) {
            logger.error("Error adding chef review: {}", e.getMessage());
            ApiResponse<ChefReviewResponse> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error adding chef review", e);
            ApiResponse<ChefReviewResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de l'ajout de l'avis sur le chef"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get all reviews for a specific chef
     * Public endpoint - no authentication required
     */
    @GetMapping("/{chefId}/reviews")
    public ResponseEntity<ApiResponse<List<ChefReviewResponse>>> getChefReviews(
            @PathVariable UUID chefId) {
        
        try {
            logger.info("Get reviews request received for chef {}", chefId);
            
            List<ChefReviewResponse> reviews = chefReviewService.getChefReviews(chefId);
            
            ApiResponse<List<ChefReviewResponse>> apiResponse = ApiResponse.success(
                    "Avis du chef récupérés avec succès",
                    reviews
            );
            
            logger.info("Retrieved {} reviews for chef {}", reviews.size(), chefId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error getting chef reviews", e);
            ApiResponse<List<ChefReviewResponse>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération des avis du chef"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get all chef reviews by the authenticated user
     */
    @GetMapping("/reviews/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<ChefReviewResponse>>> getMyChefReviews() {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Get my chef reviews request received from user {}", userId);
            
            List<ChefReviewResponse> reviews = chefReviewService.getUserReviews(userId);
            
            ApiResponse<List<ChefReviewResponse>> apiResponse = ApiResponse.success(
                    "Vos avis sur les chefs récupérés avec succès",
                    reviews
            );
            
            logger.info("Retrieved {} chef reviews for user {}", reviews.size(), userId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error getting user chef reviews", e);
            ApiResponse<List<ChefReviewResponse>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération de vos avis sur les chefs"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Check if the authenticated user has reviewed a specific chef
     */
    @GetMapping("/{chefId}/reviews/check")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Boolean>> checkUserReview(@PathVariable UUID chefId) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Check user review request received from user {} for chef {}", userId, chefId);
            
            boolean hasReviewed = chefReviewService.hasUserReviewedChef(userId, chefId);
            
            ApiResponse<Boolean> apiResponse = ApiResponse.success(
                    "Vérification effectuée avec succès",
                    hasReviewed
            );
            
            logger.info("User {} has {} reviewed chef {}", userId, hasReviewed ? "already" : "not", chefId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error checking user chef review", e);
            ApiResponse<Boolean> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la vérification"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}