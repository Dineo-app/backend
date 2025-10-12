package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.plats.dto.AddPlatReviewRequest;
import com.dineo_backend.dineo.plats.dto.PlatReviewResponse;
import com.dineo_backend.dineo.plats.model.PlatReview;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PlatReviewRepository;
import com.dineo_backend.dineo.plats.service.PlatReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PlatReviewService
 */
@Service
public class PlatReviewServiceImpl implements PlatReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(PlatReviewServiceImpl.class);
    
    @Autowired
    private PlatReviewRepository platReviewRepository;
    
    @Autowired
    private PlatRepository platRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Add a review for a plat
     */
    @Override
    @Transactional
    public PlatReviewResponse addReview(UUID userId, AddPlatReviewRequest request) {
        logger.info("Adding review for plat {} by user {}", request.getPlatId(), userId);
        
        // Verify plat exists
        if (!platRepository.existsById(request.getPlatId())) {
            logger.error("Plat not found with ID: {}", request.getPlatId());
            throw new RuntimeException("Plat non trouvé");
        }
        
        // Verify user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        // Check if user already reviewed this plat
        if (platReviewRepository.findByPlatIdAndUserId(request.getPlatId(), userId).isPresent()) {
            logger.warn("User {} already reviewed plat {}", userId, request.getPlatId());
            throw new RuntimeException("Vous avez déjà donné un avis sur ce plat");
        }
        
        // Create review
        PlatReview review = new PlatReview(
                request.getPlatId(),
                userId,
                request.getReviewText(),
                request.getRate()
        );
        
        // Save review
        PlatReview savedReview = platReviewRepository.save(review);
        
        logger.info("Review added successfully with ID: {}", savedReview.getId());
        
        // Map to response
        User user = userOptional.get();
        return mapToResponse(savedReview, user);
    }
    
    /**
     * Get all reviews for a specific plat
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlatReviewResponse> getPlatReviews(UUID platId) {
        logger.info("Fetching all reviews for plat: {}", platId);
        
        List<PlatReview> reviews = platReviewRepository.findByPlatId(platId);
        
        logger.info("Found {} reviews for plat {}", reviews.size(), platId);
        
        return reviews.stream()
                .map(this::mapToResponseWithUserFetch)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all reviews by a specific user
     */
    @Override
    @Transactional(readOnly = true)
    public List<PlatReviewResponse> getUserReviews(UUID userId) {
        logger.info("Fetching all reviews by user: {}", userId);
        
        List<PlatReview> reviews = platReviewRepository.findByUserId(userId);
        
        logger.info("Found {} reviews by user {}", reviews.size(), userId);
        
        return reviews.stream()
                .map(this::mapToResponseWithUserFetch)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a user has already reviewed a plat
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedPlat(UUID userId, UUID platId) {
        return platReviewRepository.findByPlatIdAndUserId(platId, userId).isPresent();
    }
    
    /**
     * Map PlatReview entity to response DTO with provided user
     */
    private PlatReviewResponse mapToResponse(PlatReview review, User user) {
        String userName = user.getFirstName() + " " + user.getLastName();
        
        return new PlatReviewResponse(
                review.getId(),
                review.getPlatId(),
                review.getUserId(),
                userName,
                review.getReviewText(),
                review.getRate(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
    
    /**
     * Map PlatReview entity to response DTO and fetch user information
     */
    private PlatReviewResponse mapToResponseWithUserFetch(PlatReview review) {
        // Fetch user information
        Optional<User> userOptional = userRepository.findById(review.getUserId());
        
        String userName = "Utilisateur inconnu";
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userName = user.getFirstName() + " " + user.getLastName();
        }
        
        return new PlatReviewResponse(
                review.getId(),
                review.getPlatId(),
                review.getUserId(),
                userName,
                review.getReviewText(),
                review.getRate(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
