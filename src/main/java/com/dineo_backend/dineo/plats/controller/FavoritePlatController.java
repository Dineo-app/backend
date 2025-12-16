package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.plats.dto.AddFavoritePlatRequest;
import com.dineo_backend.dineo.plats.dto.FavoritePlatResponse;
import com.dineo_backend.dineo.plats.dto.FavoritePlatWithDetailsResponse;
import com.dineo_backend.dineo.plats.service.FavoritePlatService;
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
 * REST Controller for favorite plat operations
 */
@RestController
@RequestMapping("/api/v1/favorites")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FavoritePlatController {

    private static final Logger logger = LoggerFactory.getLogger(FavoritePlatController.class);

    @Autowired
    private FavoritePlatService favoritePlatService;

    /**
     * Add a plat to user's favorites
     */
    @PostMapping("/plats")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<FavoritePlatResponse>> addPlatToFavorites(
            @Valid @RequestBody AddFavoritePlatRequest request) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Add plat to favorites request received for user ID: {}, plat ID: {}", 
                       userId, request.getPlatId());

            FavoritePlatResponse response = favoritePlatService.addPlatToFavorites(userId, request);

            ApiResponse<FavoritePlatResponse> apiResponse = ApiResponse.success(
                    "Plat ajouté aux favoris avec succès", 
                    response
            );

            logger.info("Plat {} successfully added to favorites for user {}", request.getPlatId(), userId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error adding plat to favorites: {}", e.getMessage());
            ApiResponse<FavoritePlatResponse> errorResponse = ApiResponse.error(
                    "Erreur lors de l'ajout aux favoris: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error adding plat to favorites: {}", e.getMessage(), e);
            ApiResponse<FavoritePlatResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de l'ajout aux favoris"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Remove a plat from user's favorites
     */
    @DeleteMapping("/plats/{platId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Void>> removePlatFromFavorites(@PathVariable UUID platId) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Remove plat from favorites request received for user ID: {}, plat ID: {}", 
                       userId, platId);

            favoritePlatService.removePlatFromFavorites(userId, platId);

            ApiResponse<Void> apiResponse = ApiResponse.success(
                    "Plat retiré des favoris avec succès", 
                    null
            );

            logger.info("Plat {} successfully removed from favorites for user {}", platId, userId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error removing plat from favorites: {}", e.getMessage());
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Erreur lors de la suppression des favoris: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error removing plat from favorites: {}", e.getMessage(), e);
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la suppression des favoris"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get all favorite plats for the authenticated user
     */
    @GetMapping("/plats")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<List<FavoritePlatWithDetailsResponse>>> getUserFavorites() {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Get user favorites request received for user ID: {}", userId);

            List<FavoritePlatWithDetailsResponse> favorites = favoritePlatService.getUserFavoritesWithDetails(userId);

            ApiResponse<List<FavoritePlatWithDetailsResponse>> apiResponse = ApiResponse.success(
                    "Favoris récupérés avec succès", 
                    favorites
            );

            logger.info("Successfully retrieved {} favorites for user {}", favorites.size(), userId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error getting user favorites: {}", e.getMessage(), e);
            ApiResponse<List<FavoritePlatWithDetailsResponse>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération des favoris"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Check if a specific plat is in user's favorites
     */
    @GetMapping("/plats/{platId}/check")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<java.util.Map<String, Boolean>>> checkPlatInFavorites(@PathVariable UUID platId) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = UUID.fromString(authentication.getName());
            
            logger.info("Check plat in favorites request received for user ID: {}, plat ID: {}", 
                       userId, platId);

            boolean isInFavorites = favoritePlatService.isPlatInFavorites(userId, platId);

            ApiResponse<java.util.Map<String, Boolean>> apiResponse = ApiResponse.success(
                    "Vérification effectuée avec succès", 
                    java.util.Map.of("isFavorite", isInFavorites)
            );

            logger.info("Plat {} is {} in favorites for user {}", 
                       platId, isInFavorites ? "present" : "not present", userId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error checking plat in favorites: {}", e.getMessage(), e);
            ApiResponse<java.util.Map<String, Boolean>> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la vérification"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}