package com.dineo_backend.dineo.administration.controller;

import com.dineo_backend.dineo.administration.dto.AdminStatsResponse;
import com.dineo_backend.dineo.administration.dto.ChefDetailResponse;
import com.dineo_backend.dineo.administration.dto.ChefListItemResponse;
import com.dineo_backend.dineo.administration.dto.CreateChefRequest;
import com.dineo_backend.dineo.administration.dto.CreateChefResponse;
import com.dineo_backend.dineo.administration.dto.CreatePlateForChefRequest;
import com.dineo_backend.dineo.administration.service.AdministrationService;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.plats.dto.PlatResponse;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for administration operations
 * Only accessible by users with Administrator role
 */
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
public class AdministrationController {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationController.class);

    @Autowired
    private AdministrationService administrationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Create a new chef account
     * Requires Administrator role
     * 
     * @param request the chef creation request
     * @param authHeader the authorization header containing JWT token
     * @return the created chef response
     */
    @PostMapping("/chefs")
    public ResponseEntity<ApiResponse<CreateChefResponse>> createChef(
            @Valid @RequestBody CreateChefRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Received request to create chef account for email: {}", request.getEmail());

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID adminUserId = jwtService.extractUserId(token);

            // Check if user has Administrator role
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to create chef account without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                              "Accès refusé. Seuls les administrateurs peuvent créer des comptes chef."));
            }

            // Create chef account
            CreateChefResponse response = administrationService.createChefAccount(request, adminUserId.toString());

            logger.info("Chef account created successfully for email: {} by admin: {}", 
                       request.getEmail(), adminUserId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created("Compte chef créé avec succès", response));

        } catch (RuntimeException e) {
            logger.error("Error creating chef account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating chef account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Generate a new random password (utility endpoint for admins)
     * Requires Administrator role
     * 
     * @param authHeader the authorization header containing JWT token
     * @return a randomly generated password
     */
    @GetMapping("/generate-password")
    public ResponseEntity<ApiResponse<String>> generatePassword(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Received request to generate random password");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID adminUserId = jwtService.extractUserId(token);

            // Check if user has Administrator role
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to generate password without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                              "Accès refusé. Seuls les administrateurs peuvent générer des mots de passe."));
            }

            // Generate password
            String password = administrationService.generateRandomPassword();

            logger.info("Random password generated successfully by admin: {}", adminUserId);

            return ResponseEntity.ok(ApiResponse.success("Mot de passe généré avec succès", password));

        } catch (Exception e) {
            logger.error("Unexpected error generating password: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get admin dashboard statistics
     * Requires Administrator role
     * 
     * @param authHeader the authorization header containing JWT token
     * @return admin dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getAdminStats(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Received request to get admin stats");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID adminUserId = jwtService.extractUserId(token);

            // Check if user has Administrator role
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to access admin stats without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                              "Accès refusé. Seuls les administrateurs peuvent accéder aux statistiques."));
            }

            // Get statistics
            AdminStatsResponse stats = administrationService.getAdminStats();

            logger.info("Admin stats retrieved successfully by admin: {}", adminUserId);

            return ResponseEntity.ok(ApiResponse.success("Statistiques récupérées avec succès", stats));

        } catch (Exception e) {
            logger.error("Unexpected error getting admin stats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get all chefs in the system
     * Requires Administrator role
     * 
     * @param authHeader the authorization header containing JWT token
     * @return list of all chefs with basic information
     */
    @GetMapping("/chefs")
    public ResponseEntity<ApiResponse<List<ChefListItemResponse>>> getAllChefs(
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Received request to get all chefs");

        try {
            // Extract JWT token
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);

            // Check if user has Administrator role
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to get all chefs without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                              "Accès refusé. Seuls les administrateurs peuvent accéder à cette ressource."));
            }

            // Get all chefs
            List<ChefListItemResponse> chefs = administrationService.getAllChefs();

            logger.info("All chefs retrieved successfully by admin: {}, count: {}", adminUserId, chefs.size());

            return ResponseEntity.ok(ApiResponse.success("Chefs récupérés avec succès", chefs));

        } catch (Exception e) {
            logger.error("Unexpected error getting all chefs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get detailed information about a specific chef
     * Requires Administrator role
     * 
     * @param chefId the chef ID
     * @param authHeader the authorization header containing JWT token
     * @return chef details with plates and orders
     */
    @GetMapping("/chefs/{chefId}")
    public ResponseEntity<ApiResponse<ChefDetailResponse>> getChefDetail(
            @PathVariable UUID chefId,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Received request to get chef detail for ID: {}", chefId);

        try {
            // Extract JWT token
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);

            // Check if user has Administrator role
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to get chef detail without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                              "Accès refusé. Seuls les administrateurs peuvent accéder à cette ressource."));
            }

            // Get chef detail
            ChefDetailResponse chefDetail = administrationService.getChefDetail(chefId);

            logger.info("Chef detail retrieved successfully by admin: {}, chef: {}", adminUserId, chefId);

            return ResponseEntity.ok(ApiResponse.success("Détails du chef récupérés avec succès", chefDetail));

        } catch (RuntimeException e) {
            logger.error("Error getting chef detail: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting chef detail: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Create a plate for a specific chef
     * Requires Administrator role
     * Supports multipart form data with image upload
     * 
     * @param chefId the chef ID
     * @param plateData JSON string containing plate data
     * @param image the plate image file (optional)
     * @param authHeader the authorization header containing JWT token
     * @return the created plate
     */
    @PostMapping("/chefs/{chefId}/plates")
    public ResponseEntity<ApiResponse<PlatResponse>> createPlateForChef(
            @PathVariable UUID chefId,
            @RequestParam("plateData") String plateData,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Received request to create plate for chef ID: {}", chefId);

        try {
            // Extract JWT token
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);

            // Check if user has Administrator role
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to create plate without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                              "Accès refusé. Seuls les administrateurs peuvent créer des plats."));
            }

            // Parse JSON plate data
            ObjectMapper objectMapper = new ObjectMapper();
            CreatePlateForChefRequest request = objectMapper.readValue(plateData, CreatePlateForChefRequest.class);

            // Create plate with image
            PlatResponse platResponse = administrationService.createPlateForChef(chefId, request, image);

            logger.info("Plate created successfully by admin: {}, for chef: {}, plate: {}", 
                       adminUserId, chefId, platResponse.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Plat créé avec succès", platResponse));

        } catch (RuntimeException e) {
            logger.error("Error creating plate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating plate: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Check if user has Administrator role
     * 
     * @param userId the user ID to check
     * @return true if user has Administrator role, false otherwise
     */
    private boolean hasAdministratorRole(UUID userId) {
        try {
            Optional<UserRole> userRole = roleRepository.findByUserIdAndRole(userId, Role.ADMIN);
            boolean hasAdminRole = userRole.isPresent();
            
            logger.debug("User {} has Administrator role: {}", userId, hasAdminRole);
            return hasAdminRole;
            
        } catch (Exception e) {
            logger.error("Error checking Administrator role for user {}: {}", userId, e.getMessage());
            return false;
        }
    }
}