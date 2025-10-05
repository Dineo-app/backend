package com.dineo_backend.dineo.administration.controller;

import com.dineo_backend.dineo.administration.dto.CreateChefRequest;
import com.dineo_backend.dineo.administration.dto.CreateChefResponse;
import com.dineo_backend.dineo.administration.service.AdministrationService;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for administration operations
 * Only accessible by users with Administrator role
 */
@RestController
@RequestMapping("/api/admin")
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