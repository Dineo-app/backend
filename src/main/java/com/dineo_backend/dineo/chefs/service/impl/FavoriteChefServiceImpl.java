package com.dineo_backend.dineo.chefs.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.dto.AddFavoriteChefRequest;
import com.dineo_backend.dineo.chefs.dto.FavoriteChefResponse;
import com.dineo_backend.dineo.chefs.dto.FavoriteChefWithDetailsResponse;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.model.ChefFavorite;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.chefs.repository.ChefFavoriteRepository;
import com.dineo_backend.dineo.chefs.service.FavoriteChefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of FavoriteChefService for favorite chef operations
 */
@Service
public class FavoriteChefServiceImpl implements FavoriteChefService {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteChefServiceImpl.class);

    @Autowired
    private ChefFavoriteRepository chefFavoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Override
    @Transactional
    public FavoriteChefResponse addChefToFavorites(UUID userId, AddFavoriteChefRequest request) {
        logger.info("Adding chef {} to favorites for user {}", request.getChefId(), userId);

        // Check if chef exists
        if (!userRepository.existsById(request.getChefId())) {
            throw new RuntimeException("Le chef avec l'ID " + request.getChefId() + " n'existe pas");
        }

        // Check if already in favorites
        if (chefFavoriteRepository.existsByUserIdAndChefId(userId, request.getChefId())) {
            throw new RuntimeException("Ce chef est déjà dans vos favoris");
        }

        // Create and save favorite chef
        ChefFavorite chefFavorite = new ChefFavorite(request.getChefId(), userId);
        ChefFavorite saved = chefFavoriteRepository.save(chefFavorite);

        logger.info("Chef {} successfully added to favorites for user {}", request.getChefId(), userId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeChefFromFavorites(UUID userId, UUID chefId) {
        logger.info("Removing chef {} from favorites for user {}", chefId, userId);

        // Find the favorite chef
        ChefFavorite chefFavorite = chefFavoriteRepository.findByUserIdAndChefId(userId, chefId)
                .orElseThrow(() -> new RuntimeException("Ce chef n'est pas dans vos favoris"));

        // Delete the favorite
        chefFavoriteRepository.delete(chefFavorite);

        logger.info("Chef {} successfully removed from favorites for user {}", chefId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteChefResponse> getUserFavoriteChefs(UUID userId) {
        logger.info("Getting favorite chefs for user {}", userId);

        List<ChefFavorite> favoriteChefs = chefFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        logger.info("Found {} favorite chefs for user {}", favoriteChefs.size(), userId);

        return favoriteChefs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteChefWithDetailsResponse> getUserFavoriteChefsWithDetails(UUID userId) {
        logger.info("Getting favorite chefs with details for user {}", userId);

        List<ChefFavorite> favoriteChefs = chefFavoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        logger.info("Found {} favorite chefs for user {}", favoriteChefs.size(), userId);

        return favoriteChefs.stream()
                .map(this::mapToDetailedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isChefInFavorites(UUID userId, UUID chefId) {
        return chefFavoriteRepository.existsByUserIdAndChefId(userId, chefId);
    }

    /**
     * Helper method to map ChefFavorite entity to FavoriteChefResponse DTO
     */
    private FavoriteChefResponse mapToResponse(ChefFavorite chefFavorite) {
        return new FavoriteChefResponse(
                chefFavorite.getId(),
                chefFavorite.getChefId(),
                chefFavorite.getUserId(),
                chefFavorite.getCreatedAt(),
                chefFavorite.getUpdatedAt()
        );
    }

    /**
     * Helper method to map ChefFavorite entity to FavoriteChefWithDetailsResponse DTO
     * Fetches complete chef information
     */
    private FavoriteChefWithDetailsResponse mapToDetailedResponse(ChefFavorite chefFavorite) {
        // Fetch the chef user information
        User chef = userRepository.findById(chefFavorite.getChefId())
                .orElseThrow(() -> new RuntimeException("Chef utilisateur non trouvé avec l'ID: " + chefFavorite.getChefId()));

        // Fetch the chef description information
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefFavorite.getChefId())
                .orElse(null); // Chef description might not exist

        return new FavoriteChefWithDetailsResponse(
                chefFavorite.getId(),
                chefFavorite.getCreatedAt(),
                chef.getId(),
                chef.getFirstName(),
                chef.getLastName(),
                chef.getPhone(),
                chef.getAddress(),
                chefDescription != null ? chefDescription.getId() : null,
                chefDescription != null ? chefDescription.getDescription() : null,
                chefDescription != null ? chefDescription.getCategories() : null,
                chefDescription != null ? chefDescription.getChefCertifications() : null,
                chefDescription != null ? chefDescription.getChefCoverImg() : null,
                chef.getCreatedAt(),
                chef.getUpdatedAt()
        );
    }
}