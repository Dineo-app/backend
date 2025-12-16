package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.chefs.dto.AddFavoriteChefRequest;
import com.dineo_backend.dineo.chefs.dto.FavoriteChefResponse;
import com.dineo_backend.dineo.chefs.dto.FavoriteChefWithDetailsResponse;
import com.dineo_backend.dineo.chefs.service.FavoriteChefService;
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
 * REST Controller for favorite chef operations
 */
@RestController
@RequestMapping("/api/v1/favorites")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FavoriteChefController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteChefController.class);

    @Autowired
    private FavoriteChefService favoriteChefService;

    /**
     * Add a chef to user's favorites
     */
    @PostMapping("/chefs")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<FavoriteChefResponse>> addChefToFavorites(
            @Valid @RequestBody AddFavoriteChefRequest request) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Add chef to favorites request received for user ID: {}, chef ID: {}", 
                       userId, request.getChefId());

            FavoriteChefResponse response = favoriteChefService.addChefToFavorites(userId, request);

            ApiResponse<FavoriteChefResponse> apiResponse = ApiResponse.success(
                    "Chef ajouté aux favoris avec succès", 
                    response
            );

            logger.info("Chef {} successfully added to favorites for user {}", request.getChefId(), userId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error adding chef to favorites: {}", e.getMessage());
            ApiResponse<FavoriteChefResponse> errorResponse = ApiResponse.error(
                    "Erreur lors de l'ajout aux favoris: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error adding chef to favorites: {}", e.getMessage(), e);
            ApiResponse<FavoriteChefResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de l'ajout aux favoris"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Remove a chef from user's favorites
     */
    @DeleteMapping("/chefs/{chefId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> removeChefFromFavorites(@PathVariable UUID chefId) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Remove chef from favorites request received for user ID: {}, chef ID: {}", 
                       userId, chefId);

            favoriteChefService.removeChefFromFavorites(userId, chefId);

            ApiResponse<Void> apiResponse = ApiResponse.success(
                    "Chef retiré des favoris avec succès", 
                    null
            );

            logger.info("Chef {} successfully removed from favorites for user {}", chefId, userId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error removing chef from favorites: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Erreur lors de la suppression des favoris: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error removing chef from favorites: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la suppression des favoris"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get all favorite chefs for the authenticated user
     */
    @GetMapping("/chefs")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<FavoriteChefWithDetailsResponse>>> getUserFavoriteChefs() {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Get user favorite chefs request received for user ID: {}", userId);

            List<FavoriteChefWithDetailsResponse> favoriteChefs = favoriteChefService.getUserFavoriteChefsWithDetails(userId);

            ApiResponse<List<FavoriteChefWithDetailsResponse>> apiResponse = ApiResponse.success(
                    "Chefs favoris récupérés avec succès", 
                    favoriteChefs
            );

            logger.info("Successfully retrieved {} favorite chefs for user {}", favoriteChefs.size(), userId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error getting user favorite chefs: {}", e.getMessage(), e);
            ApiResponse<List<FavoriteChefWithDetailsResponse>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération des chefs favoris"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Check if a specific chef is in user's favorites
     */
    @GetMapping("/chefs/{chefId}/check")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Boolean>>> checkChefInFavorites(@PathVariable UUID chefId) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Check chef in favorites request received for user ID: {}, chef ID: {}", 
                       userId, chefId);

            boolean isInFavorites = favoriteChefService.isChefInFavorites(userId, chefId);

            ApiResponse<java.util.Map<String, Boolean>> apiResponse = ApiResponse.success(
                    "Vérification effectuée avec succès", 
                    java.util.Map.of("isFavorite", isInFavorites)
            );

            logger.info("Chef {} is {} in favorites for user {}", 
                       chefId, isInFavorites ? "present" : "not present", userId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error checking chef in favorites: {}", e.getMessage(), e);
            ApiResponse<java.util.Map<String, Boolean>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la vérification"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}