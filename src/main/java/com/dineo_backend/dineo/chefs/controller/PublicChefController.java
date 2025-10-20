package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.dto.PublicChefResponse;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.model.ChefReview;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.chefs.repository.ChefReviewRepository;
import com.dineo_backend.dineo.plats.dto.PublicPlatResponse;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PlatReview;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PlatReviewRepository;
import com.dineo_backend.dineo.plats.repository.PromotionPlatRepository;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Public REST Controller for Chef operations
 * These endpoints don't require authentication and are used for public chef information
 */
@RestController
@RequestMapping("/api/v1/public/chefs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicChefController {

    private static final Logger logger = LoggerFactory.getLogger(PublicChefController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Autowired
    private ChefReviewRepository chefReviewRepository;

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private PlatReviewRepository platReviewRepository;

    @Autowired
    private PromotionPlatRepository promotionRepository;

    /**
     * Get chef details by ID
     * Public endpoint - no authentication required
     */
    @GetMapping("/{chefId}")
    public ResponseEntity<ApiResponse<PublicChefResponse>> getChefById(@PathVariable UUID chefId) {
        try {
            logger.info("Get chef details request received for chef {}", chefId);

            // Get user info
            Optional<User> userOpt = userRepository.findById(chefId);
            if (userOpt.isEmpty()) {
                ApiResponse<PublicChefResponse> apiResponse = ApiResponse.error(
                    "Chef non trouvé"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }

            User user = userOpt.get();
            
            // Get chef description
            Optional<ChefDescription> chefDescOpt = chefDescriptionRepository.findByUserId(chefId);
            if (chefDescOpt.isEmpty()) {
                ApiResponse<PublicChefResponse> apiResponse = ApiResponse.error(
                    "Profil de chef non trouvé"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }
            
            // Calculate chef average rating and total reviews
            List<ChefReview> reviews = chefReviewRepository.findByChefId(chefId);
            Double averageRating = calculateChefAverageRating(reviews);
            Integer totalReviews = reviews.size();

            // Create response
            PublicChefResponse response = new PublicChefResponse();
            response.setId(user.getId());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setAddress(user.getAddress());
            response.setCreatedAt(user.getCreatedAt());
            response.setAverageRating(averageRating);
            response.setTotalReviews(totalReviews);

            if (chefDescOpt.isPresent()) {
                ChefDescription chefDesc = chefDescOpt.get();
                response.setChefCoverImg(chefDesc.getChefCoverImg());
                response.setDescription(chefDesc.getDescription());
                response.setCategories(chefDesc.getCategories());
                response.setChefCertifications(chefDesc.getChefCertifications());
            }

            ApiResponse<PublicChefResponse> apiResponse = ApiResponse.success(
                "Détails du chef récupérés avec succès",
                response
            );

            logger.info("Successfully retrieved chef details for chef {}", chefId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error getting chef details", e);
            ApiResponse<PublicChefResponse> apiResponse = ApiResponse.error(
                "Erreur lors de la récupération des détails du chef"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Get chef's plates
     * Public endpoint - no authentication required
     */
    @GetMapping("/{chefId}/plats")
    public ResponseEntity<ApiResponse<List<PublicPlatResponse>>> getChefPlats(@PathVariable UUID chefId) {
        try {
            logger.info("Get chef plats request received for chef {}", chefId);

            // Check if chef exists
            Optional<User> userOpt = userRepository.findById(chefId);
            if (userOpt.isEmpty()) {
                ApiResponse<List<PublicPlatResponse>> apiResponse = ApiResponse.error(
                    "Chef non trouvé"
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
            }

            // Get all available plats by chef
            List<Plat> plats = platRepository.findByChefId(chefId).stream()
                .filter(Plat::getAvailable)
                .collect(Collectors.toList());
            
            List<PublicPlatResponse> platResponses = plats.stream().map(plat -> {
                // Get promotion if exists
                Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(plat.getId(), LocalDateTime.now());
                
                return mapToPublicResponse(plat, promotion.orElse(null));
            }).collect(Collectors.toList());

            ApiResponse<List<PublicPlatResponse>> apiResponse = ApiResponse.success(
                "Plats du chef récupérés avec succès",
                platResponses
            );

            logger.info("Successfully retrieved {} plats for chef {}", platResponses.size(), chefId);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error getting chef plats", e);
            ApiResponse<List<PublicPlatResponse>> apiResponse = ApiResponse.error(
                "Erreur lors de la récupération des plats du chef"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    /**
     * Helper method to map Plat to PublicPlatResponse
     */
    private PublicPlatResponse mapToPublicResponse(Plat plat, PromotionPlat promotion) {
        PublicPlatResponse response = new PublicPlatResponse();
        response.setId(plat.getId());
        response.setName(plat.getName());
        response.setDescription(plat.getDescription());
        response.setEstimatedCookTime(plat.getEstimatedCookTime());
        response.setPrice(plat.getPrice());
        response.setCategories(plat.getCategories());
        response.setImageUrl(plat.getImageUrl());
        response.setCreatedAt(plat.getCreatedAt());
        response.setUpdatedAt(plat.getUpdatedAt());

        // Add promotion if exists
        if (promotion != null) {
            PromotionResponse promoResponse = new PromotionResponse(
                promotion.getId(),
                promotion.getPlatId(),
                promotion.getReductionValue(),
                promotion.getReductionEnds(),
                promotion.getIsActive(),
                promotion.getCreatedAt(),
                promotion.getUpdatedAt()
            );
            response.setPromotion(promoResponse);
        }

        // Calculate and add average rating
        Double averageRating = calculatePlatAverageRating(plat.getId());
        response.setAverageRating(averageRating);

        // Add chef information
        PublicPlatResponse.ChefInfo chefInfo = getChefInfo(plat.getChefId());
        response.setChef(chefInfo);

        return response;
    }

    /**
     * Helper method to calculate chef average rating
     */
    private Double calculateChefAverageRating(List<ChefReview> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream()
            .mapToInt(ChefReview::getRate)
            .sum();

        return Math.round((sum / reviews.size()) * 10.0) / 10.0; // Round to 1 decimal place
    }

    /**
     * Helper method to calculate plat average rating
     */
    private Double calculatePlatAverageRating(UUID platId) {
        List<PlatReview> reviews = platReviewRepository.findByPlatId(platId);
        
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream()
            .mapToInt(PlatReview::getRate)
            .sum();

        return Math.round((sum / reviews.size()) * 10.0) / 10.0; // Round to 1 decimal place
    }

    /**
     * Helper method to get chef information
     */
    private PublicPlatResponse.ChefInfo getChefInfo(UUID chefId) {
        // Get user info
        Optional<User> userOpt = userRepository.findById(chefId);
        Optional<ChefDescription> chefDescOpt = chefDescriptionRepository.findByUserId(chefId);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String chefCoverImg = chefDescOpt.map(ChefDescription::getChefCoverImg).orElse(null);
            
            // Calculate chef average rating
            List<ChefReview> reviews = chefReviewRepository.findByChefId(chefId);
            Double chefAverageRating = calculateChefAverageRating(reviews);

            return new PublicPlatResponse.ChefInfo(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                chefCoverImg,
                chefAverageRating
            );
        }

        // Return default chef info if user not found
        return new PublicPlatResponse.ChefInfo(chefId, "Chef", "Unknown", null, 0.0);
    }
}