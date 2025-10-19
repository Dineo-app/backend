package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.plats.dto.CreatePlatRequest;
import com.dineo_backend.dineo.plats.dto.CreatePlatResponse;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;
import com.dineo_backend.dineo.plats.dto.UpdatePlatRequest;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PromotionPlatRepository;
import com.dineo_backend.dineo.plats.service.BunnyCdnService;
import com.dineo_backend.dineo.plats.service.PlatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    @Autowired
    private PromotionPlatRepository promotionRepository;

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
            plat.setPrice(request.getPrice());
            plat.setCategories(request.getCategories());
            plat.setImageUrl(imageUrl);
            plat.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);

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
                savedPlat.getPrice(),
                savedPlat.getCategories(),
                savedPlat.getImageUrl(),
                savedPlat.getCreatedAt(),
                savedPlat.getUpdatedAt()
            );
            response.setAvailable(savedPlat.getAvailable());

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
                .map(plat -> {
                    CreatePlatResponse response = new CreatePlatResponse(
                        plat.getId(),
                        plat.getChefId(),
                        plat.getName(),
                        plat.getDescription(),
                        plat.getEstimatedCookTime(),
                        plat.getPrice(),
                        plat.getCategories(),
                        plat.getImageUrl(),
                        plat.getCreatedAt(),
                        plat.getUpdatedAt()
                    );
                    response.setAvailable(plat.getAvailable());
                    
                    // Get active promotion for this plat
                    Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                        plat.getId(), 
                        LocalDateTime.now()
                    );
                    
                    promotion.ifPresent(promo -> {
                        PromotionResponse promoResponse = new PromotionResponse(
                            promo.getId(),
                            promo.getPlatId(),
                            promo.getReductionValue(),
                            promo.getReductionEnds(),
                            promo.getIsActive(),
                            promo.getCreatedAt(),
                            promo.getUpdatedAt()
                        );
                        response.setPromotion(promoResponse);
                    });
                    
                    return response;
                })
                .collect(Collectors.toList());

            logger.info("Successfully converted {} plats to response DTOs for chef: {}", responseList.size(), chefUserId);
            return responseList;

        } catch (Exception e) {
            logger.error("Error getting plats for chef {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des plats: " + e.getMessage());
        }
    }

    @Override
    public List<String> getChefCategories(UUID chefUserId) {
        try {
            logger.info("Getting unique categories for chef: {}", chefUserId);

            // Validate that user is a chef
            if (!isUserAChef(chefUserId)) {
                throw new RuntimeException("Utilisateur non autorisé. Seuls les chefs peuvent accéder à cette ressource.");
            }

            // Get all plats for the chef
            List<Plat> plats = platRepository.findByChefIdOrderByCreatedAtDesc(chefUserId);
            
            // Extract all categories and flatten to unique list
            List<String> uniqueCategories = plats.stream()
                .filter(plat -> plat.getCategories() != null && !plat.getCategories().isEmpty())
                .flatMap(plat -> plat.getCategories().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            logger.info("Found {} unique categories for chef: {}", uniqueCategories.size(), chefUserId);
            return uniqueCategories;

        } catch (Exception e) {
            logger.error("Error getting categories for chef {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des catégories: " + e.getMessage());
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

    @Override
    public CreatePlatResponse getPlatById(UUID platId) {
        logger.info("Getting plat details for ID: {}", platId);

        try {
            // Find plat by ID
            Optional<Plat> platOptional = platRepository.findById(platId);
            
            if (platOptional.isEmpty()) {
                logger.error("Plat {} not found", platId);
                throw new RuntimeException("Plat non trouvé.");
            }

            Plat plat = platOptional.get();
            logger.info("Found plat '{}' with ID: {}", plat.getName(), platId);

            // Convert to response DTO
            CreatePlatResponse response = new CreatePlatResponse();
            response.setId(plat.getId());
            response.setName(plat.getName());
            response.setDescription(plat.getDescription());
            response.setEstimatedCookTime(plat.getEstimatedCookTime());
            response.setPrice(plat.getPrice());
            response.setCategories(plat.getCategories());
            response.setImageUrl(plat.getImageUrl());
            response.setAvailable(plat.getAvailable());
            response.setCreatedAt(plat.getCreatedAt());
            response.setUpdatedAt(plat.getUpdatedAt());
            response.setChefId(plat.getChefId());

            // Get active promotion for this plat
            Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                plat.getId(), 
                LocalDateTime.now()
            );
            
            promotion.ifPresent(promo -> {
                PromotionResponse promoResponse = new PromotionResponse(
                    promo.getId(),
                    promo.getPlatId(),
                    promo.getReductionValue(),
                    promo.getReductionEnds(),
                    promo.getIsActive(),
                    promo.getCreatedAt(),
                    promo.getUpdatedAt()
                );
                response.setPromotion(promoResponse);
            });

            logger.info("Plat {} ('{}') retrieved successfully", platId, plat.getName());
            return response;

        } catch (RuntimeException e) {
            // Re-throw business logic exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error getting plat {}: {}", platId, e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération du plat: " + e.getMessage());
        }
    }

    @Override
    public CreatePlatResponse updatePlat(UUID platId, UpdatePlatRequest request, MultipartFile imageFile, UUID chefUserId) {
        logger.info("Updating plat {} for chef: {}", platId, chefUserId);

        // Validate that user is a chef (has PROVIDER role)
        if (!isUserAChef(chefUserId)) {
            logger.error("User {} is not authorized to update plats. Must have PROVIDER role.", chefUserId);
            throw new RuntimeException("Accès refusé. Seuls les chefs peuvent modifier des plats.");
        }

        try {
            // Check if plat exists and belongs to the chef
            Optional<Plat> platOptional = platRepository.findByIdAndChefId(platId, chefUserId);
            
            if (platOptional.isEmpty()) {
                logger.error("Plat {} not found or doesn't belong to chef {}", platId, chefUserId);
                throw new RuntimeException("Plat non trouvé ou vous n'avez pas l'autorisation de le modifier.");
            }

            Plat plat = platOptional.get();
            logger.info("Found plat '{}' belonging to chef {}, proceeding with update", plat.getName(), chefUserId);

            // Update fields if provided
            if (request.getName() != null && !request.getName().trim().isEmpty()) {
                plat.setName(request.getName());
            }
            if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
                plat.setDescription(request.getDescription());
            }
            if (request.getEstimatedCookTime() != null) {
                plat.setEstimatedCookTime(request.getEstimatedCookTime());
            }
            if (request.getPrice() != null) {
                plat.setPrice(request.getPrice());
            }
            if (request.getCategories() != null && !request.getCategories().isEmpty()) {
                plat.setCategories(request.getCategories());
            }
            if (request.getAvailable() != null) {
                plat.setAvailable(request.getAvailable());
            }

            // Upload new image if provided, otherwise keep existing image
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.info("Uploading new image for plat: {}", plat.getName());
                String imageUrl = bunnyCdnService.uploadImage(imageFile, "plats");
                plat.setImageUrl(imageUrl);
                logger.info("Image uploaded successfully: {}", imageUrl);
            } else {
                logger.info("No new image provided, keeping existing image for plat: {}", plat.getName());
            }

            // Save updated plat
            Plat updatedPlat = platRepository.save(plat);
            logger.info("Plat {} updated successfully by chef: {}", platId, chefUserId);

            // Create response
            CreatePlatResponse response = new CreatePlatResponse();
            response.setId(updatedPlat.getId());
            response.setChefId(updatedPlat.getChefId());
            response.setName(updatedPlat.getName());
            response.setDescription(updatedPlat.getDescription());
            response.setEstimatedCookTime(updatedPlat.getEstimatedCookTime());
            response.setPrice(updatedPlat.getPrice());
            response.setCategories(updatedPlat.getCategories());
            response.setImageUrl(updatedPlat.getImageUrl());
            response.setAvailable(updatedPlat.getAvailable());
            response.setCreatedAt(updatedPlat.getCreatedAt());
            response.setUpdatedAt(updatedPlat.getUpdatedAt());

            return response;

        } catch (RuntimeException e) {
            // Re-throw business logic exceptions
            throw e;
        } catch (Exception e) {
            logger.error("Error updating plat {} for chef {}: {}", platId, chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du plat: " + e.getMessage());
        }
    }
}