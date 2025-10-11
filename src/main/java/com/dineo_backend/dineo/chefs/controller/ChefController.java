package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileRequest;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileResponse;
import com.dineo_backend.dineo.chefs.service.ChefService;
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

import java.util.UUID;

/**
 * REST Controller for chef-related operations
 */
@RestController
@RequestMapping("/api/v1/chefs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChefController {

    private static final Logger logger = LoggerFactory.getLogger(ChefController.class);

    @Autowired
    private ChefService chefService;

    /**
     * Update chef profile (user information + chef description)
     * Only the authenticated chef can update their own profile
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<UpdateChefProfileResponse>> updateChefProfile(
            @Valid @RequestBody UpdateChefProfileRequest request) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID chefUserId = UUID.fromString(authentication.getName());
            
            logger.info("Chef profile update request received for user ID: {}", chefUserId);

            UpdateChefProfileResponse response = chefService.updateChefProfile(chefUserId, request);

            ApiResponse<UpdateChefProfileResponse> apiResponse = ApiResponse.success(
                    "Profil du chef mis à jour avec succès", 
                    response
            );

            logger.info("Chef profile updated successfully for user ID: {}", chefUserId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error updating chef profile: {}", e.getMessage());
            ApiResponse<UpdateChefProfileResponse> errorResponse = ApiResponse.error(
                    "Erreur lors de la mise à jour du profil: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error updating chef profile: {}", e.getMessage(), e);
            ApiResponse<UpdateChefProfileResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la mise à jour du profil"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}