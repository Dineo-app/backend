package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.plats.dto.CreatePlatRequest;
import com.dineo_backend.dineo.plats.dto.CreatePlatResponse;
import com.dineo_backend.dineo.plats.service.PlatService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Plat operations
 * Handles plat creation and management for chefs
 */
@RestController
@RequestMapping("/api/v1/plats")
@CrossOrigin(origins = "*")
public class PlatController {

    private static final Logger logger = LoggerFactory.getLogger(PlatController.class);

    @Autowired
    private PlatService platService;

    @Autowired
    private JwtService jwtService;

    /**
     * Create a new plat with image upload
     * Requires PROVIDER role (chef)
     * 
     * @param platData JSON data containing plat information
     * @param image the plat image file (optional)
     * @param authHeader the authorization header containing JWT token
     * @return the created plat response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreatePlatResponse>> createPlat(
            @RequestParam("platData") String platData,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Received plat creation request");

            // Extract and validate JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.error("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AppConstants.JWT_TOKEN_MISSING));
            }

            String token = authHeader.substring(7);
            
            if (!jwtService.isAccessToken(token) || jwtService.isTokenExpired(token)) {
                logger.error("Invalid or expired JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID));
            }

            // Extract user ID from token
            UUID chefUserId = jwtService.extractUserId(token);
            logger.info("Creating plat for chef: {}", chefUserId);

            // Parse JSON plat data
            ObjectMapper objectMapper = new ObjectMapper();
            CreatePlatRequest request = objectMapper.readValue(platData, CreatePlatRequest.class);

            // Validate request (manual validation since @Valid doesn't work with @RequestParam)
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Le nom du plat est obligatoire"));
            }
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "La description du plat est obligatoire"));
            }
            if (request.getEstimatedCookTime() == null || request.getEstimatedCookTime() <= 0) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Le temps de cuisson doit être positif"));
            }
            if (request.getCategories() == null || request.getCategories().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Au moins une catégorie est requise"));
            }

            logger.info("Creating plat with name: {}", request.getName());

            // Create plat
            CreatePlatResponse response = platService.createPlat(request, image, chefUserId);

            logger.info("Plat created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Plat créé avec succès", response));

        } catch (RuntimeException e) {
            logger.error("Business error creating plat: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating plat: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur interne du serveur"));
        }
    }

    /**
     * Get all plats created by the authenticated chef
     * Requires PROVIDER role (chef)
     * 
     * @param authHeader the authorization header containing JWT token
     * @return list of chef's plats
     */
    @GetMapping("/my-plats")
    public ResponseEntity<ApiResponse<List<CreatePlatResponse>>> getMyPlats(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Received request to get chef plats");

            // Extract and validate JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.error("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AppConstants.JWT_TOKEN_MISSING));
            }

            String token = authHeader.substring(7);
            
            if (!jwtService.isAccessToken(token) || jwtService.isTokenExpired(token)) {
                logger.error("Invalid or expired JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID));
            }

            // Extract user ID from token
            UUID chefUserId = jwtService.extractUserId(token);
            logger.info("Getting plats for chef: {}", chefUserId);

            // Get chef's plats
            List<CreatePlatResponse> chefPlats = platService.getChefPlats(chefUserId);

            logger.info("Retrieved {} plats for chef: {}", chefPlats.size(), chefUserId);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.PLATS_RETRIEVED_SUCCESS, chefPlats));

        } catch (RuntimeException e) {
            logger.error("Business error getting chef plats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting chef plats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur interne du serveur"));
        }
    }

    /**
     * Delete a plat by ID
     * Only the chef who created the plat can delete it
     * Requires PROVIDER role (chef)
     * 
     * @param platId the ID of the plat to delete
     * @param authHeader the authorization header containing JWT token
     * @return success response if deleted
     */
    @DeleteMapping("/{platId}")
    public ResponseEntity<ApiResponse<String>> deletePlat(
            @PathVariable UUID platId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Received request to delete plat: {}", platId);

            // Extract and validate JWT token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.error("Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AppConstants.JWT_TOKEN_MISSING));
            }

            String token = authHeader.substring(7);
            
            if (!jwtService.isAccessToken(token) || jwtService.isTokenExpired(token)) {
                logger.error("Invalid or expired JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, AppConstants.JWT_TOKEN_INVALID));
            }

            // Extract user ID from token
            UUID chefUserId = jwtService.extractUserId(token);
            logger.info("Deleting plat {} for chef: {}", platId, chefUserId);

            // Delete plat
            platService.deletePlat(platId, chefUserId);

            logger.info("Plat {} deleted successfully by chef: {}", platId, chefUserId);
            return ResponseEntity.ok(ApiResponse.success(AppConstants.PLAT_DELETED_SUCCESS, "Plat supprimé"));

        } catch (RuntimeException e) {
            logger.error("Business error deleting plat {}: {}", platId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error deleting plat {}: {}", platId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur interne du serveur"));
        }
    }

    /**
     * Health check endpoint for plat service
     * 
     * @return simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("Service plats opérationnel", "OK"));
    }
}