package com.dineo_backend.dineo.chefs.service;

import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileRequest;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileResponse;

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
}