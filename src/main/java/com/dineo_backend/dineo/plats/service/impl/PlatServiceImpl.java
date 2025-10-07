package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.plats.dto.CreatePlatRequest;
import com.dineo_backend.dineo.plats.dto.CreatePlatResponse;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.service.BunnyCdnService;
import com.dineo_backend.dineo.plats.service.PlatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PlatService
 * Handles plat creation with image upload and role validation
 */
@Service
@Transactional
public class PlatServiceImpl implements PlatService {

    private static final Logger logger = LoggerFactory.getLogger(PlatServiceImpl.class);

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BunnyCdnService bunnyCdnService;

    @Override
    public CreatePlatResponse createPlat(CreatePlatRequest request, MultipartFile imageFile, UUID chefUserId) {
        logger.info("Creating plat for chef: {}. Plat name: {}", chefUserId, request.getName());

        // Validate that user is a chef (has PROVIDER role)
        if (!isUserAChef(chefUserId)) {
            logger.error("User {} is not authorized to create plats. Must have PROVIDER role.", chefUserId);
            throw new RuntimeException("Accès refusé. Seuls les chefs peuvent créer des plats.");
        }

        try {
            // Upload image to Bunny CDN
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.info("Uploading image for plat: {}", request.getName());
                imageUrl = bunnyCdnService.uploadImage(imageFile, "plats");
                logger.info("Image uploaded successfully: {}", imageUrl);
            } else {
                logger.info("No image provided for plat: {}", request.getName());
            }

            // Create plat entity
            Plat plat = new Plat();
            plat.setChefId(chefUserId);
            plat.setName(request.getName());
            plat.setDescription(request.getDescription());
            plat.setEstimatedCookTime(request.getEstimatedCookTime());
            plat.setCategories(request.getCategories());
            plat.setImageUrl(imageUrl);

            // Save plat
            Plat savedPlat = platRepository.save(plat);
            logger.info("Plat created successfully with ID: {}", savedPlat.getId());

            // Create response
            CreatePlatResponse response = new CreatePlatResponse(
                savedPlat.getId(),
                savedPlat.getChefId(),
                savedPlat.getName(),
                savedPlat.getDescription(),
                savedPlat.getEstimatedCookTime(),
                savedPlat.getCategories(),
                savedPlat.getImageUrl(),
                savedPlat.getCreatedAt(),
                savedPlat.getUpdatedAt()
            );

            logger.info("Plat creation completed successfully for chef: {}", chefUserId);
            return response;

        } catch (Exception e) {
            logger.error("Error creating plat for chef {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la création du plat: " + e.getMessage());
        }
    }

    @Override
    public List<CreatePlatResponse> getChefPlats(UUID chefUserId) {
        logger.info("Getting plats for chef: {}", chefUserId);

        // Validate that user is a chef (has PROVIDER role)
        if (!isUserAChef(chefUserId)) {
            logger.error("User {} is not authorized to access plats. Must have PROVIDER role.", chefUserId);
            throw new RuntimeException("Accès refusé. Seuls les chefs peuvent accéder à leurs plats.");
        }

        try {
            // Get all plats for the chef
            List<Plat> chefPlats = platRepository.findByChefIdOrderByCreatedAtDesc(chefUserId);
            logger.info("Found {} plats for chef: {}", chefPlats.size(), chefUserId);

            // Convert to response DTOs
            List<CreatePlatResponse> responseList = chefPlats.stream()
                .map(plat -> new CreatePlatResponse(
                    plat.getId(),
                    plat.getChefId(),
                    plat.getName(),
                    plat.getDescription(),
                    plat.getEstimatedCookTime(),
                    plat.getCategories(),
                    plat.getImageUrl(),
                    plat.getCreatedAt(),
                    plat.getUpdatedAt()
                ))
                .collect(Collectors.toList());

            logger.info("Successfully converted {} plats to response DTOs for chef: {}", responseList.size(), chefUserId);
            return responseList;

        } catch (Exception e) {
            logger.error("Error getting plats for chef {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des plats: " + e.getMessage());
        }
    }

    @Override
    public boolean isUserAChef(UUID userId) {
        try {
            return roleRepository.findByUserIdAndRole(userId, Role.PROVIDER).isPresent();
        } catch (Exception e) {
            logger.error("Error checking chef role for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    public void deletePlat(UUID platId, UUID chefUserId) {
        logger.info("Deleting plat {} for chef: {}", platId, chefUserId);

        // Validate that user is a chef (has PROVIDER role)
        if (!isUserAChef(chefUserId)) {
            logger.error("User {} is not authorized to delete plats. Must have PROVIDER role.", chefUserId);
            throw new RuntimeException("Accès refusé. Seuls les chefs peuvent supprimer des plats.");
        }

        try {
            // Check if plat exists and belongs to the chef
            Optional<Plat> platOptional = platRepository.findByIdAndChefId(platId, chefUserId);
            
            if (platOptional.isEmpty()) {
                logger.error("Plat {} not found or doesn't belong to chef {}", platId, chefUserId);
                throw new RuntimeException("Plat non trouvé ou vous n'avez pas l'autorisation de le supprimer.");
            }

            Plat plat = platOptional.get();
            logger.info("Found plat '{}' belonging to chef {}, proceeding with deletion", plat.getName(), chefUserId);

            // Delete the plat
            platRepository.delete(plat);
            logger.info("Plat {} ('{}') deleted successfully by chef: {}", platId, plat.getName(), chefUserId);

        } catch (RuntimeException e) {
            // Re-throw business logic exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting plat {} for chef {}: {}", platId, chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression du plat: " + e.getMessage());
        }
    }
}