package com.dineo_backend.dineo.administration.service.impl;

import com.dineo_backend.dineo.administration.dto.AdminStatsResponse;
import com.dineo_backend.dineo.administration.dto.ChefDetailResponse;
import com.dineo_backend.dineo.administration.dto.ChefListItemResponse;
import com.dineo_backend.dineo.administration.dto.CreateChefRequest;
import com.dineo_backend.dineo.administration.dto.CreateChefResponse;
import com.dineo_backend.dineo.administration.dto.CreatePlateForChefRequest;
import com.dineo_backend.dineo.administration.service.AdministrationService;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.plats.dto.PlatResponse;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.service.BunnyCdnService;
import com.dineo_backend.dineo.sms.service.OvhSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private PlatRepository platRepository;

    @Autowired
    private OvhSmsService smsService;

    @Autowired
    private BunnyCdnService bunnyCdnService;

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

    @Override
    public AdminStatsResponse getAdminStats() {
        logger.info("Fetching admin statistics");

        try {
            // Count total users
            long totalUsers = userRepository.count();
            logger.debug("Total users: {}", totalUsers);

            // Count chefs (users with PROVIDER role)
            long totalChefs = roleRepository.countByRole(Role.PROVIDER);
            logger.debug("Total chefs: {}", totalChefs);

            // Count total dishes (plats)
            long totalDishes = platRepository.count();
            logger.debug("Total dishes: {}", totalDishes);

            // For now, set orders to 0 as we don't have order repository yet
            long totalOrders = 0L;
            long todayOrders = 0L;
            
            logger.info("Admin statistics retrieved successfully");

            return AdminStatsResponse.builder()
                    .totalUsers(totalUsers)
                    .totalChefs(totalChefs)
                    .totalDishes(totalDishes)
                    .totalOrders(totalOrders)
                    .todayOrders(todayOrders)
                    .build();

        } catch (Exception e) {
            logger.error("Error fetching admin statistics: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des statistiques");
        }
    }

    @Override
    public List<ChefListItemResponse> getAllChefs() {
        logger.info("Fetching all chefs for admin view");

        try {
            // Get all users with PROVIDER role
            List<UserRole> chefRoles = roleRepository.findByRole(Role.PROVIDER);
            
            List<ChefListItemResponse> chefs = new ArrayList<>();
            
            for (UserRole userRole : chefRoles) {
                UUID chefUserId = userRole.getUserId();
                
                // Get the user
                Optional<User> chefOpt = userRepository.findById(chefUserId);
                if (chefOpt.isEmpty()) {
                    continue;
                }
                
                User chef = chefOpt.get();
                
                // Count plates for this chef
                int totalPlates = platRepository.findByChefId(chef.getId()).size();
                
                // Orders set to 0 (placeholder)
                int totalOrders = 0;
                
                ChefListItemResponse chefItem = new ChefListItemResponse(
                    chef.getId(),
                    chef.getFirstName(),
                    chef.getLastName(),
                    chef.getEmail(),
                    chef.getPhone(),
                    chef.getAddress(),
                    chef.isVerified(),
                    totalPlates,
                    totalOrders,
                    chef.getCreatedAt()
                );
                
                chefs.add(chefItem);
            }
            
            logger.info("Successfully fetched {} chefs", chefs.size());
            return chefs;
            
        } catch (Exception e) {
            logger.error("Error fetching all chefs: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des chefs");
        }
    }

    @Override
    public ChefDetailResponse getChefDetail(UUID chefId) {
        logger.info("Fetching chef detail for ID: {}", chefId);

        try {
            // Find chef user
            User chef = userRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef introuvable"));
            
            // Verify user is a chef
            Optional<UserRole> chefRole = roleRepository.findByUserIdAndRole(chefId, Role.PROVIDER);
            if (chefRole.isEmpty()) {
                logger.error("User {} is not a chef", chefId);
                throw new RuntimeException("L'utilisateur n'est pas un chef");
            }
            
            // Get all plates for this chef
            List<Plat> plats = platRepository.findByChefId(chefId);
            List<PlatResponse> platResponses = plats.stream()
                .map(this::convertPlatToResponse)
                .collect(Collectors.toList());
            
            // Orders placeholders
            int totalOrders = 0;
            int completedOrders = 0;
            int activeOrders = 0;
            
            ChefDetailResponse response = new ChefDetailResponse(
                chef.getId(),
                chef.getFirstName(),
                chef.getLastName(),
                chef.getEmail(),
                chef.getPhone(),
                chef.getAddress(),
                chef.isVerified(),
                chef.getCreatedAt(),
                chef.getUpdatedAt(),
                platResponses,
                totalOrders,
                completedOrders,
                activeOrders
            );
            
            logger.info("Successfully fetched chef detail with {} plates", platResponses.size());
            return response;
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching chef detail: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des détails du chef");
        }
    }

    @Override
    public PlatResponse createPlateForChef(UUID chefId, CreatePlateForChefRequest request, MultipartFile image) {
        logger.info("Admin creating plate for chef ID: {}", chefId);

        try {
            // Verify chef exists and has PROVIDER role
            User chef = userRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef introuvable"));
            
            Optional<UserRole> chefRole = roleRepository.findByUserIdAndRole(chefId, Role.PROVIDER);
            if (chefRole.isEmpty()) {
                logger.error("User {} is not a chef", chefId);
                throw new RuntimeException("L'utilisateur n'est pas un chef");
            }
            
            // Upload image if provided
            String imageUrl = null;
            if (image != null && !image.isEmpty()) {
                logger.info("Uploading plate image for chef: {}", chefId);
                imageUrl = bunnyCdnService.uploadImage(image, "plats");
            }
            
            // Create new plate
            Plat plat = new Plat();
            plat.setChefId(chefId);
            plat.setName(request.getName());
            plat.setDescription(request.getDescription());
            plat.setEstimatedCookTime(request.getEstimatedCookTime());
            plat.setPrice(request.getPrice());
            plat.setCategories(request.getCategories());
            plat.setImageUrl(imageUrl != null ? imageUrl : request.getImageUrl());
            plat.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
            
            Plat savedPlat = platRepository.save(plat);
            logger.info("Plate created successfully with ID: {}", savedPlat.getId());
            
            return convertPlatToResponse(savedPlat);
            
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating plate for chef: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la création du plat");
        }
    }

    /**
     * Helper method to convert Plat entity to PlatResponse DTO
     */
    private PlatResponse convertPlatToResponse(Plat plat) {
        PlatResponse response = new PlatResponse();
        response.setId(plat.getId());
        response.setChefId(plat.getChefId());
        response.setName(plat.getName());
        response.setDescription(plat.getDescription());
        response.setEstimatedCookTime(plat.getEstimatedCookTime());
        response.setPrice(plat.getPrice());
        response.setCategories(plat.getCategories());
        response.setImageUrl(plat.getImageUrl());
        response.setAvailable(plat.getAvailable());
        response.setCreatedAt(plat.getCreatedAt());
        response.setUpdatedAt(plat.getUpdatedAt());
        return response;
    }
}