package com.dineo_backend.dineo.chefs.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.dto.AddChefReviewRequest;
import com.dineo_backend.dineo.chefs.dto.ChefReviewResponse;
import com.dineo_backend.dineo.chefs.model.ChefReview;
import com.dineo_backend.dineo.chefs.repository.ChefReviewRepository;
import com.dineo_backend.dineo.chefs.service.ChefReviewService;
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
 * Implementation of ChefReviewService
 */
@Service
public class ChefReviewServiceImpl implements ChefReviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(ChefReviewServiceImpl.class);
    
    @Autowired
    private ChefReviewRepository chefReviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Add a review for a chef
     */
    @Override
    @Transactional
    public ChefReviewResponse addReview(UUID userId, AddChefReviewRequest request) {
        logger.info("Adding review for chef {} by user {}", request.getChefId(), userId);
        
        // Verify chef exists (chef is a user with PROVIDER role)
        if (!userRepository.existsById(request.getChefId())) {
            logger.error("Chef not found with ID: {}", request.getChefId());
            throw new RuntimeException("Chef non trouvé");
        }
        
        // Verify reviewer exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            logger.error("User not found with ID: {}", userId);
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        // Check if user already reviewed this chef
        if (chefReviewRepository.findByChefIdAndUserId(request.getChefId(), userId).isPresent()) {
            logger.warn("User {} already reviewed chef {}", userId, request.getChefId());
            throw new RuntimeException("Vous avez déjà donné un avis sur ce chef");
        }
        
        // Create review
        ChefReview review = new ChefReview(
                request.getChefId(),
                userId,
                request.getReviewText(),
                request.getRate()
        );
        
        // Save review
        ChefReview savedReview = chefReviewRepository.save(review);
        
        logger.info("Review added successfully with ID: {}", savedReview.getId());
        
        // Map to response
        User user = userOptional.get();
        return mapToResponse(savedReview, user);
    }
    
    /**
     * Get all reviews for a specific chef
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChefReviewResponse> getChefReviews(UUID chefId) {
        logger.info("Fetching all reviews for chef: {}", chefId);
        
        List<ChefReview> reviews = chefReviewRepository.findByChefId(chefId);
        
        logger.info("Found {} reviews for chef {}", reviews.size(), chefId);
        
        return reviews.stream()
                .map(this::mapToResponseWithUserFetch)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all reviews by a specific user
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChefReviewResponse> getUserReviews(UUID userId) {
        logger.info("Fetching all chef reviews by user: {}", userId);
        
        List<ChefReview> reviews = chefReviewRepository.findByUserId(userId);
        
        logger.info("Found {} chef reviews by user {}", reviews.size(), userId);
        
        return reviews.stream()
                .map(this::mapToResponseWithUserFetch)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a user has already reviewed a chef
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedChef(UUID userId, UUID chefId) {
        return chefReviewRepository.findByChefIdAndUserId(chefId, userId).isPresent();
    }
    
    /**
     * Map ChefReview entity to response DTO with provided user
     */
    private ChefReviewResponse mapToResponse(ChefReview review, User user) {
        String userName = user.getFirstName() + " " + user.getLastName();
        
        return new ChefReviewResponse(
                review.getId(),
                review.getChefId(),
                review.getUserId(),
                userName,
                review.getReviewText(),
                review.getRate(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
    
    /**
     * Map ChefReview entity to response DTO and fetch user information
     */
    private ChefReviewResponse mapToResponseWithUserFetch(ChefReview review) {
        // Fetch user information
        Optional<User> userOptional = userRepository.findById(review.getUserId());
        
        String userName = "Utilisateur inconnu";
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userName = user.getFirstName() + " " + user.getLastName();
        }
        
        return new ChefReviewResponse(
                review.getId(),
                review.getChefId(),
                review.getUserId(),
                userName,
                review.getReviewText(),
                review.getRate(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}