package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.plats.dto.AddPlatReviewRequest;
import com.dineo_backend.dineo.plats.dto.PlatReviewResponse;
import com.dineo_backend.dineo.plats.service.PlatReviewService;
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
 * REST Controller for plat review operations
 */
@RestController
@RequestMapping("/api/v1/plats")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PlatReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(PlatReviewController.class);
    
    @Autowired
    private PlatReviewService platReviewService;
    
    /**
     * Add a review for a plat
     * Only authenticated users can add reviews
     */
    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<PlatReviewResponse>> addReview(
            @Valid @RequestBody AddPlatReviewRequest request) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Add review request received from user {} for plat {}", userId, request.getPlatId());
            
            PlatReviewResponse response = platReviewService.addReview(userId, request);
            
            ApiResponse<PlatReviewResponse> apiResponse = ApiResponse.success(
                    "Avis ajouté avec succès",
                    response
            );
            
            logger.info("Review added successfully for plat {} by user {}", request.getPlatId(), userId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (RuntimeException e) {
            logger.error("Error adding review: {}", e.getMessage());
            ApiResponse<PlatReviewResponse> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error adding review", e);
            ApiResponse<PlatReviewResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de l'ajout de l'avis"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get all reviews for a specific plat
     * Public endpoint - no authentication required
     */
    @GetMapping("/{platId}/reviews")
    public ResponseEntity<ApiResponse<List<PlatReviewResponse>>> getPlatReviews(
            @PathVariable UUID platId) {
        
        try {
            logger.info("Get reviews request received for plat {}", platId);
            
            List<PlatReviewResponse> reviews = platReviewService.getPlatReviews(platId);
            
            ApiResponse<List<PlatReviewResponse>> apiResponse = ApiResponse.success(
                    "Avis récupérés avec succès",
                    reviews
            );
            
            logger.info("Retrieved {} reviews for plat {}", reviews.size(), platId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error getting plat reviews", e);
            ApiResponse<List<PlatReviewResponse>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération des avis"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get all reviews by the authenticated user
     */
    @GetMapping("/reviews/my-reviews")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<PlatReviewResponse>>> getMyReviews() {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Get my reviews request received from user {}", userId);
            
            List<PlatReviewResponse> reviews = platReviewService.getUserReviews(userId);
            
            ApiResponse<List<PlatReviewResponse>> apiResponse = ApiResponse.success(
                    "Vos avis récupérés avec succès",
                    reviews
            );
            
            logger.info("Retrieved {} reviews for user {}", reviews.size(), userId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error getting user reviews", e);
            ApiResponse<List<PlatReviewResponse>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération de vos avis"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Check if the authenticated user has reviewed a specific plat
     */
    @GetMapping("/{platId}/reviews/check")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Boolean>> checkUserReview(@PathVariable UUID platId) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Check user review request received from user {} for plat {}", userId, platId);
            
            boolean hasReviewed = platReviewService.hasUserReviewedPlat(userId, platId);
            
            ApiResponse<Boolean> apiResponse = ApiResponse.success(
                    "Vérification effectuée avec succès",
                    hasReviewed
            );
            
            logger.info("User {} has {} reviewed plat {}", userId, hasReviewed ? "already" : "not", platId);
            return ResponseEntity.ok(apiResponse);
            
        } catch (Exception e) {
            logger.error("Unexpected error checking user review", e);
            ApiResponse<Boolean> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la vérification"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
