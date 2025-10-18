package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.chefs.dto.DeleteCertificationResponse;
import com.dineo_backend.dineo.chefs.dto.GetChefProfileResponse;
import com.dineo_backend.dineo.chefs.dto.UpdateChefCoverImageResponse;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileRequest;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileResponse;
import com.dineo_backend.dineo.chefs.dto.UploadCertificationResponse;
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
import org.springframework.web.multipart.MultipartFile;

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
     * Get chef profile (user information + chef description + cover image + certifications)
     * Only the authenticated chef can retrieve their own profile
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<GetChefProfileResponse>> getChefProfile() {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID chefUserId = UUID.fromString(authentication.getName());
            
            logger.info("Chef profile retrieval request received for user ID: {}", chefUserId);

            GetChefProfileResponse response = chefService.getChefProfile(chefUserId);

            ApiResponse<GetChefProfileResponse> apiResponse = ApiResponse.success(
                    "Profil du chef récupéré avec succès", 
                    response
            );

            logger.info("Chef profile retrieved successfully for user ID: {}", chefUserId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error retrieving chef profile: {}", e.getMessage());
            ApiResponse<GetChefProfileResponse> errorResponse = ApiResponse.error(
                    "Erreur lors de la récupération du profil: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error retrieving chef profile: {}", e.getMessage(), e);
            ApiResponse<GetChefProfileResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la récupération du profil"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

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

    /**
     * Update chef cover image
     * Only the authenticated chef can update their own cover image
     */
    @PutMapping("/cover-image")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<UpdateChefCoverImageResponse>> updateChefCoverImage(
            @RequestParam("image") MultipartFile imageFile) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID chefUserId = UUID.fromString(authentication.getName());
            
            logger.info("Chef cover image update request received for user ID: {}", chefUserId);

            // Validate file
            if (imageFile == null || imageFile.isEmpty()) {
                ApiResponse<UpdateChefCoverImageResponse> errorResponse = ApiResponse.error(
                        "Aucun fichier image fourni"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file type
            String contentType = imageFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                ApiResponse<UpdateChefCoverImageResponse> errorResponse = ApiResponse.error(
                        "Le fichier doit être une image"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file size (max 10MB)
            if (imageFile.getSize() > 10 * 1024 * 1024) {
                ApiResponse<UpdateChefCoverImageResponse> errorResponse = ApiResponse.error(
                        "La taille du fichier ne doit pas dépasser 10MB"
                );
                return ResponseEntity.badRequest().body(errorResponse);
            }

            UpdateChefCoverImageResponse response = chefService.updateChefCoverImage(chefUserId, imageFile);

            ApiResponse<UpdateChefCoverImageResponse> apiResponse = ApiResponse.success(
                    "Image de couverture mise à jour avec succès", 
                    response
            );

            logger.info("Chef cover image updated successfully for user ID: {}", chefUserId);
            return ResponseEntity.ok(apiResponse);

        } catch (RuntimeException e) {
            logger.error("Error updating chef cover image: {}", e.getMessage());
            ApiResponse<UpdateChefCoverImageResponse> errorResponse = ApiResponse.error(
                    "Erreur lors de la mise à jour de l'image: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error updating chef cover image: {}", e.getMessage(), e);
            ApiResponse<UpdateChefCoverImageResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la mise à jour de l'image"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Upload certification image for chef
     * Restricted to PROVIDER role only
     */
    @PostMapping("/certifications")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<UploadCertificationResponse>> uploadCertificationImage(
            @RequestParam("certificationImage") MultipartFile certificationImage) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userIdStr = authentication.getName();
            UUID userId = UUID.fromString(userIdStr);

            logger.info("Uploading certification image for chef user ID: {}", userId);

            // Call service to upload certification image
            UploadCertificationResponse response = chefService.uploadCertificationImage(userId, certificationImage);

            // Return success response
            ApiResponse<UploadCertificationResponse> apiResponse = ApiResponse.success(
                    "Image de certification téléchargée avec succès",
                    response
            );
            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for certification image upload: {}", e.getMessage());
            ApiResponse<UploadCertificationResponse> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (RuntimeException e) {
            logger.error("Runtime error uploading certification image: {}", e.getMessage());
            ApiResponse<UploadCertificationResponse> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error uploading certification image: {}", e.getMessage(), e);
            ApiResponse<UploadCertificationResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors du téléchargement"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Delete certification image for chef
     * Restricted to PROVIDER role only
     */
    @DeleteMapping("/certifications")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<DeleteCertificationResponse>> deleteCertificationImage(
            @RequestParam("certificationImageUrl") String certificationImageUrl) {
        
        try {
            // Get authenticated user ID
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userIdStr = authentication.getName();
            UUID userId = UUID.fromString(userIdStr);

            logger.info("Deleting certification image for chef user ID: {} with URL: {}", userId, certificationImageUrl);

            // Call service to delete certification image
            DeleteCertificationResponse response = chefService.deleteCertificationImage(userId, certificationImageUrl);

            // Return success response
            ApiResponse<DeleteCertificationResponse> apiResponse = ApiResponse.success(
                    "Image de certification supprimée avec succès",
                    response
            );
            return ResponseEntity.ok(apiResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for certification image deletion: {}", e.getMessage());
            ApiResponse<DeleteCertificationResponse> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (RuntimeException e) {
            logger.error("Runtime error deleting certification image: {}", e.getMessage());
            ApiResponse<DeleteCertificationResponse> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Unexpected error deleting certification image: {}", e.getMessage(), e);
            ApiResponse<DeleteCertificationResponse> errorResponse = ApiResponse.error(
                    "Une erreur inattendue s'est produite lors de la suppression"
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}