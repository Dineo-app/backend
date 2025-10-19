package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.plats.dto.CreatePromotionRequest;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PromotionPlatRepository;
import com.dineo_backend.dineo.plats.service.PromotionPlatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PromotionPlatService
 */
@Service
public class PromotionPlatServiceImpl implements PromotionPlatService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionPlatServiceImpl.class);

    @Autowired
    private PromotionPlatRepository promotionRepository;

    @Autowired
    private PlatRepository platRepository;

    @Override
    @Transactional
    public PromotionResponse createPromotion(UUID chefUserId, CreatePromotionRequest request) {
        logger.info("Creating promotion for plat ID: {} by chef user ID: {}", request.getPlatId(), chefUserId);

        // Verify plat exists and belongs to the chef
        Plat plat = platRepository.findById(request.getPlatId())
                .orElseThrow(() -> new RuntimeException("Plat non trouvé avec l'ID: " + request.getPlatId()));

        if (!plat.getChefId().equals(chefUserId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à créer une promotion pour ce plat");
        }

        // Check if there's already an active promotion for this plat
        promotionRepository.findActivePromotionByPlatId(request.getPlatId(), LocalDateTime.now())
                .ifPresent(existingPromotion -> {
                    throw new RuntimeException("Ce plat a déjà une promotion active");
                });

        // Validate reduction end date
        if (request.getReductionEnds().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("La date de fin doit être dans le futur");
        }

        // Create promotion
        PromotionPlat promotion = new PromotionPlat(
                request.getPlatId(),
                request.getReductionValue(),
                request.getReductionEnds()
        );

        PromotionPlat savedPromotion = promotionRepository.save(promotion);
        logger.info("Promotion created successfully with ID: {}", savedPromotion.getId());

        return mapToResponse(savedPromotion);
    }

    @Override
    public PromotionResponse getActivePromotionByPlatId(UUID platId) {
        logger.info("Getting active promotion for plat ID: {}", platId);

        return promotionRepository.findActivePromotionByPlatId(platId, LocalDateTime.now())
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public List<PromotionResponse> getChefPromotions(UUID chefUserId) {
        logger.info("Getting all promotions for chef user ID: {}", chefUserId);

        // Get all plats for the chef
        List<Plat> chefPlats = platRepository.findByChefIdOrderByCreatedAtDesc(chefUserId);
        
        if (chefPlats.isEmpty()) {
            return new ArrayList<>();
        }

        // Get promotions for all chef's plats
        List<UUID> platIds = chefPlats.stream()
                .map(Plat::getId)
                .collect(Collectors.toList());

        List<PromotionPlat> promotions = new ArrayList<>();
        for (UUID platId : platIds) {
            promotions.addAll(promotionRepository.findByPlatIdOrderByCreatedAtDesc(platId));
        }

        return promotions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePromotion(UUID chefUserId, UUID promotionId) {
        logger.info("Deleting promotion ID: {} by chef user ID: {}", promotionId, chefUserId);

        PromotionPlat promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion non trouvée avec l'ID: " + promotionId));

        // Verify the plat belongs to the chef
        Plat plat = platRepository.findById(promotion.getPlatId())
                .orElseThrow(() -> new RuntimeException("Plat non trouvé"));

        if (!plat.getChefId().equals(chefUserId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette promotion");
        }

        promotionRepository.delete(promotion);
        logger.info("Promotion deleted successfully with ID: {}", promotionId);
    }

    @Override
    @Transactional
    public int deactivateExpiredPromotions() {
        logger.info("Running scheduled task to deactivate expired promotions");

        List<PromotionPlat> expiredPromotions = promotionRepository.findExpiredActivePromotions(LocalDateTime.now());
        
        if (expiredPromotions.isEmpty()) {
            logger.info("No expired promotions found");
            return 0;
        }

        int count = 0;
        for (PromotionPlat promotion : expiredPromotions) {
            promotion.setIsActive(false);
            promotionRepository.save(promotion);
            count++;
            logger.info("Deactivated expired promotion ID: {} for plat ID: {}", 
                    promotion.getId(), promotion.getPlatId());
        }

        logger.info("Deactivated {} expired promotions", count);
        return count;
    }

    /**
     * Helper method to map entity to response DTO
     */
    private PromotionResponse mapToResponse(PromotionPlat promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getPlatId(),
                promotion.getReductionValue(),
                promotion.getReductionEnds(),
                promotion.getIsActive(),
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
        );
    }
}
