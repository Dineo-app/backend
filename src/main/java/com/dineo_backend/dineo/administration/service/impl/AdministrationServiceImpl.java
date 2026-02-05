package com.dineo_backend.dineo.administration.service.impl;

import com.dineo_backend.dineo.administration.dto.CreateChefRequest;
import com.dineo_backend.dineo.administration.dto.CreateChefResponse;
import com.dineo_backend.dineo.administration.service.AdministrationService;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.sms.service.OvhSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of AdministrationService
 * Handles chef account creation with SMS notification
 */
@Service
@Transactional
public class AdministrationServiceImpl implements AdministrationService {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Autowired
    private OvhSmsService smsService;

    @Override
    public CreateChefResponse createChefAccount(CreateChefRequest request, String adminUserId) {
        logger.info("Creating chef account for phone: {} by admin: {}", request.getPhone(), adminUserId);

        // Check if user with email already exists
        Optional<User> existingUserByEmail = userRepository.findByEmail(request.getEmail());
        if (existingUserByEmail.isPresent()) {
            logger.error("User with email {} already exists", request.getEmail());
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Check if user with phone already exists
        Optional<User> existingUserByPhone = userRepository.findByPhone(request.getPhone());
        if (existingUserByPhone.isPresent()) {
            logger.error("User with phone {} already exists", request.getPhone());
            throw new RuntimeException("Un utilisateur avec ce numéro de téléphone existe déjà");
        }

        // Create User entity
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setVerified(true); // Admin-created accounts are pre-verified

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        // Create UserRole with PROVIDER role
        UserRole userRole = new UserRole(savedUser.getId(), Role.PROVIDER);
        roleRepository.save(userRole);
        logger.info("PROVIDER role assigned to user: {}", savedUser.getId());

        // Create ChefDescription
        ChefDescription chefDescription = new ChefDescription();
        chefDescription.setUserId(savedUser.getId());
        chefDescription.setDescription(request.getDescription());
        chefDescription.setCategories(request.getCategories());

        ChefDescription savedChefDescription = chefDescriptionRepository.save(chefDescription);
        logger.info("Chef description created successfully with ID: {}", savedChefDescription.getId());

        // Send welcome SMS notification in French
        boolean smsSent = sendWelcomeSms(
            savedUser.getPhone(), 
            savedUser.getFirstName(), 
            savedUser.getLastName()
        );

        // Create response
        CreateChefResponse response = new CreateChefResponse(
            savedUser.getId(),
            savedUser.getFirstName(),
            savedUser.getLastName(),
            savedUser.getEmail(),
            savedUser.getPhone(),
            savedUser.getAddress(),
            savedUser.getCreatedAt(),
            savedChefDescription.getId(),
            savedChefDescription.getDescription(),
            savedChefDescription.getCategories(),
            "N/A", // Passwordless authentication
            smsSent
        );

        logger.info("Chef account created successfully for: {} ({})", savedUser.getPhone(), savedUser.getEmail());
        return response;
    }

    /**
     * Send welcome SMS to newly created chef
     * Message in French for better user experience
     */
    private boolean sendWelcomeSms(String phone, String firstName, String lastName) {
        try {
            logger.info("Sending welcome SMS to chef: {}", phone);

            String message = String.format(
                "Bonjour %s %s ! 🎉\n\n" +
                "Félicitations ! Votre compte chef Miamlo a été créé avec succès.\n\n" +
                "Vous pouvez maintenant vous connecter à l'application avec votre numéro de téléphone et commencer à partager vos délicieuses créations culinaires.\n\n" +
                "Bienvenue dans la famille Miamlo ! 👨‍🍳\n\n" +
                "L'équipe Miamlo",
                firstName, lastName
            );

            smsService.sendSms(phone, message);
            logger.info("Welcome SMS sent successfully to: {}", phone);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send welcome SMS to: {}. Error: {}", phone, e.getMessage());
            return false;
        }
    }

    @Override
    public String generateRandomPassword() {
        // This method is deprecated - passwordless authentication is used
        logger.warn("generateRandomPassword() called but passwordless authentication is active");
        return "N/A";
    }

    @Override
    public boolean sendWelcomeEmail(String email, String password, String firstName, String lastName) {
        // This method is deprecated - SMS notification is used instead
        logger.warn("sendWelcomeEmail() called but SMS notification is used instead");
        return false;
    }
}