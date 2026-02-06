package com.dineo_backend.dineo.administration.service;

import com.dineo_backend.dineo.administration.dto.AdminStatsResponse;
import com.dineo_backend.dineo.administration.dto.ChefDetailResponse;
import com.dineo_backend.dineo.administration.dto.ChefListItemResponse;
import com.dineo_backend.dineo.administration.dto.CreateChefRequest;
import com.dineo_backend.dineo.administration.dto.CreateChefResponse;
import com.dineo_backend.dineo.administration.dto.CreatePlateForChefRequest;
import com.dineo_backend.dineo.plats.dto.PlatResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for administration operations
 * Handles chef account creation and management by administrators
 */
public interface AdministrationService {

    /**
     * Create a new chef account with auto-generated password
     * Only accessible by users with Administrator role
     * 
     * @param request the chef creation request containing user and chef description info
     * @param adminUserId the ID of the administrator creating the chef account
     * @return the created chef response with user info and temporary password
     */
    CreateChefResponse createChefAccount(CreateChefRequest request, String adminUserId);

    /**
     * Generate a secure random password
     * 
     * @return a randomly generated password
     */
    String generateRandomPassword();

    /**
     * Send welcome email with login credentials to the newly created chef
     * 
     * @param email the chef's email address
     * @param password the generated password
     * @param firstName the chef's first name
     * @param lastName the chef's last name
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendWelcomeEmail(String email, String password, String firstName, String lastName);

    /**
     * Get admin dashboard statistics
     * 
     * @return admin statistics including user, chef, dish, and order counts
     */
    AdminStatsResponse getAdminStats();

    /**
     * Get all chefs in the system with basic information
     * 
     * @return list of chefs with basic details
     */
    List<ChefListItemResponse> getAllChefs();

    /**
     * Get detailed information about a specific chef including their plates and orders
     * 
     * @param chefId the ID of the chef
     * @return chef detail response with all information
     */
    ChefDetailResponse getChefDetail(UUID chefId);

    /**
     * Create a new plate for a specific chef (admin function)
     * 
     * @param chefId the ID of the chef
     * @param request the plate creation request
     * @param image the plate image file (optional)
     * @return the created plate response
     */
    PlatResponse createPlateForChef(UUID chefId, CreatePlateForChefRequest request, MultipartFile image);
}