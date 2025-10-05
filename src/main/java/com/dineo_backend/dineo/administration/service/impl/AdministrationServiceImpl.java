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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Implementation of AdministrationService
 * Handles chef account creation with email notification
 */
@Service
@Transactional
public class AdministrationServiceImpl implements AdministrationService {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationServiceImpl.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 12;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public CreateChefResponse createChefAccount(CreateChefRequest request, String adminUserId) {
        logger.info("Creating chef account for email: {} by admin: {}", request.getEmail(), adminUserId);

        // Check if user with email already exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            logger.error("User with email {} already exists", request.getEmail());
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Generate random password
        String generatedPassword = generateRandomPassword();
        logger.info("Generated password for chef: {}", request.getEmail());

        // Create User entity
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setIsVerified(false); // Chef will verify on first login
        user.setIsActive(true);

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
        // chefCertifications will be empty list by default - chef can add them later

        ChefDescription savedChefDescription = chefDescriptionRepository.save(chefDescription);
        logger.info("Chef description created successfully with ID: {}", savedChefDescription.getId());

        // Send welcome email
        boolean emailSent = sendWelcomeEmail(
            savedUser.getEmail(), 
            generatedPassword, 
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
            savedUser.getIsVerified(),
            savedUser.getIsActive(),
            savedUser.getCreatedAt(),
            savedChefDescription.getId(),
            savedChefDescription.getDescription(),
            savedChefDescription.getCategories(),
            generatedPassword, // Include for admin notification (won't be logged)
            emailSent
        );

        logger.info("Chef account created successfully for: {}", request.getEmail());
        return response;
    }

    @Override
    public String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        logger.debug("Random password generated successfully");
        return password.toString();
    }

    @Override
    public boolean sendWelcomeEmail(String email, String password, String firstName, String lastName) {
        try {
            logger.info("Sending welcome email to: {}", email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Bienvenue chez Dineo - Vos identifiants de connexion");
            
            String emailBody = String.format(
                "Bonjour %s %s,\n\n" +
                "Bienvenue chez Dineo ! Votre compte chef a été créé avec succès.\n\n" +
                "Voici vos identifiants de connexion :\n" +
                "Email : %s\n" +
                "Mot de passe temporaire : %s\n\n" +
                "Pour des raisons de sécurité, nous vous recommandons fortement de changer votre mot de passe lors de votre première connexion.\n\n" +
                "Vous pouvez maintenant vous connecter à l'application Dineo et commencer à gérer vos plats et commandes.\n\n" +
                "Si vous avez des questions, n'hésitez pas à nous contacter.\n\n" +
                "Cordialement,\n" +
                "L'équipe Dineo",
                firstName, lastName, email, password
            );
            
            message.setText(emailBody);
            mailSender.send(message);

            logger.info("Welcome email sent successfully to: {}", email);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}. Error: {}", email, e.getMessage());
            return false;
        }
    }
}