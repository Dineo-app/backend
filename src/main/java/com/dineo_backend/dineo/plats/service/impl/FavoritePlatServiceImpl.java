package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.plats.dto.AddFavoritePlatRequest;
import com.dineo_backend.dineo.plats.dto.FavoritePlatResponse;
import com.dineo_backend.dineo.plats.dto.FavoritePlatWithDetailsResponse;
import com.dineo_backend.dineo.plats.model.FavoritePlat;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.repository.FavoritePlatRepository;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.service.FavoritePlatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of FavoritePlatService for favorite plat operations
 */
@Service
public class FavoritePlatServiceImpl implements FavoritePlatService {

    private static final Logger logger = LoggerFactory.getLogger(FavoritePlatServiceImpl.class);

    @Autowired
    private FavoritePlatRepository favoritePlatRepository;

    @Autowired
    private PlatRepository platRepository;

    @Override
    @Transactional
    public FavoritePlatResponse addPlatToFavorites(UUID userId, AddFavoritePlatRequest request) {
        logger.info("Adding plat {} to favorites for user {}", request.getPlatId(), userId);

        // Check if plat exists
        if (!platRepository.existsById(request.getPlatId())) {
            throw new RuntimeException("Le plat avec l'ID " + request.getPlatId() + " n'existe pas");
        }

        // Check if already in favorites
        if (favoritePlatRepository.existsByUserIdAndPlatId(userId, request.getPlatId())) {
            throw new RuntimeException("Ce plat est déjà dans vos favoris");
        }

        // Create and save favorite plat
        FavoritePlat favoritePlat = new FavoritePlat(request.getPlatId(), userId);
        FavoritePlat saved = favoritePlatRepository.save(favoritePlat);

        logger.info("Plat {} successfully added to favorites for user {}", request.getPlatId(), userId);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removePlatFromFavorites(UUID userId, UUID platId) {
        logger.info("Removing plat {} from favorites for user {}", platId, userId);

        // Find the favorite plat
        FavoritePlat favoritePlat = favoritePlatRepository.findByUserIdAndPlatId(userId, platId)
                .orElseThrow(() -> new RuntimeException("Ce plat n'est pas dans vos favoris"));

        // Delete the favorite
        favoritePlatRepository.delete(favoritePlat);

        logger.info("Plat {} successfully removed from favorites for user {}", platId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoritePlatResponse> getUserFavorites(UUID userId) {
        logger.info("Getting favorite plats for user {}", userId);

        List<FavoritePlat> favoritePlats = favoritePlatRepository.findByUserIdOrderByCreatedAtDesc(userId);

        logger.info("Found {} favorite plats for user {}", favoritePlats.size(), userId);

        return favoritePlats.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoritePlatWithDetailsResponse> getUserFavoritesWithDetails(UUID userId) {
        logger.info("Getting favorite plats with details for user {}", userId);

        List<FavoritePlat> favoritePlats = favoritePlatRepository.findByUserIdOrderByCreatedAtDesc(userId);

        logger.info("Found {} favorite plats for user {}", favoritePlats.size(), userId);

        return favoritePlats.stream()
                .map(this::mapToDetailedResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPlatInFavorites(UUID userId, UUID platId) {
        return favoritePlatRepository.existsByUserIdAndPlatId(userId, platId);
    }

    /**
     * Helper method to map FavoritePlat entity to FavoritePlatResponse DTO
     */
    private FavoritePlatResponse mapToResponse(FavoritePlat favoritePlat) {
        return new FavoritePlatResponse(
                favoritePlat.getId(),
                favoritePlat.getPlatId(),
                favoritePlat.getUserId(),
                favoritePlat.getCreatedAt(),
                favoritePlat.getUpdatedAt()
        );
    }

    /**
     * Helper method to map FavoritePlat entity to FavoritePlatWithDetailsResponse DTO
     * Fetches complete plat information
     */
    private FavoritePlatWithDetailsResponse mapToDetailedResponse(FavoritePlat favoritePlat) {
        // Fetch the complete plat information
        Plat plat = platRepository.findById(favoritePlat.getPlatId())
                .orElseThrow(() -> new RuntimeException("Plat non trouvé avec l'ID: " + favoritePlat.getPlatId()));

        return new FavoritePlatWithDetailsResponse(
                favoritePlat.getId(),
                favoritePlat.getCreatedAt(),
                plat.getId(),
                plat.getChefId(),
                plat.getName(),
                plat.getDescription(),
                plat.getEstimatedCookTime(),
                plat.getCategories(),
                plat.getImageUrl(),
                plat.getCreatedAt(),
                plat.getUpdatedAt()
        );
    }
}