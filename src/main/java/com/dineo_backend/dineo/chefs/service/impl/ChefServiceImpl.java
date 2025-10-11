package com.dineo_backend.dineo.chefs.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileRequest;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileResponse;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.chefs.service.ChefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of ChefService for chef-related operations
 */
@Service
public class ChefServiceImpl implements ChefService {

    private static final Logger logger = LoggerFactory.getLogger(ChefServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Override
    @Transactional
    public UpdateChefProfileResponse updateChefProfile(UUID chefUserId, UpdateChefProfileRequest request) {
        logger.info("Updating chef profile for user ID: {}", chefUserId);

        // Find and update user information
        User user = userRepository.findById(chefUserId)
                .orElseThrow(() -> new RuntimeException("Chef utilisateur non trouvé avec l'ID: " + chefUserId));

        // Check if email is already taken by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
        }

        // Update user fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        logger.info("User information updated successfully for user ID: {}", chefUserId);

        // Find and update chef description
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefUserId)
                .orElseThrow(() -> new RuntimeException("Description du chef non trouvée pour l'utilisateur ID: " + chefUserId));

        // Update chef description fields
        chefDescription.setDescription(request.getDescription());
        chefDescription.setCategories(request.getCategories());

        ChefDescription updatedChefDescription = chefDescriptionRepository.save(chefDescription);
        logger.info("Chef description updated successfully for user ID: {}", chefUserId);

        // Create and return response
        UpdateChefProfileResponse response = new UpdateChefProfileResponse(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getAddress(),
                updatedUser.getUpdatedAt(),
                updatedChefDescription.getId(),
                updatedChefDescription.getDescription(),
                updatedChefDescription.getCategories(),
                updatedChefDescription.getUpdatedAt()
        );

        logger.info("Chef profile update completed successfully for user ID: {}", chefUserId);
        return response;
    }
}