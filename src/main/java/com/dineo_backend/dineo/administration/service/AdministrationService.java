package com.dineo_backend.dineo.administration.service;

import com.dineo_backend.dineo.administration.dto.CreateChefRequest;
import com.dineo_backend.dineo.administration.dto.CreateChefResponse;

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
}