package com.dineo_backend.dineo.chefs.service;

import com.dineo_backend.dineo.chefs.dto.DeleteCertificationResponse;
import com.dineo_backend.dineo.chefs.dto.UpdateChefCoverImageResponse;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileRequest;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileResponse;
import com.dineo_backend.dineo.chefs.dto.UploadCertificationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for chef-related operations
 */
public interface ChefService {

    /**
     * Update chef profile including user information and chef description
     * 
     * @param chefUserId The UUID of the chef user
     * @param request The update request containing the new information
     * @return Updated chef profile response
     */
    UpdateChefProfileResponse updateChefProfile(UUID chefUserId, UpdateChefProfileRequest request);

    /**
     * Update chef cover image
     * 
     * @param chefUserId The UUID of the chef user
     * @param imageFile The cover image file to upload
     * @return Update chef cover image response with new image URL
     */
    UpdateChefCoverImageResponse updateChefCoverImage(UUID chefUserId, MultipartFile imageFile);

    /**
     * Upload a chef certification image
     * 
     * @param chefUserId The UUID of the chef user
     * @param certificationImage The certification image file to upload
     * @return Upload certification response with new image URL
     */
    UploadCertificationResponse uploadCertificationImage(UUID chefUserId, MultipartFile certificationImage);

    /**
     * Delete a specific chef certification image
     * 
     * @param chefUserId The UUID of the chef user
     * @param certificationImageUrl The URL of the certification image to delete
     * @return Delete certification response with deleted image URL
     */
    DeleteCertificationResponse deleteCertificationImage(UUID chefUserId, String certificationImageUrl);
}