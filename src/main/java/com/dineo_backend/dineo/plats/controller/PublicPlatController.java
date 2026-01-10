package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.model.ChefReview;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.chefs.repository.ChefReviewRepository;
import com.dineo_backend.dineo.common.PaginatedResponse;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;
import com.dineo_backend.dineo.plats.dto.PublicPlatResponse;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PlatReview;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PlatReviewRepository;
import com.dineo_backend.dineo.plats.repository.PromotionPlatRepository;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.dineo_backend.dineo.common.util.GeocodingUtil;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Public REST Controller for Plat operations
 * No authentication required - for public user access
 */
@RestController
@RequestMapping("/api/v1/public/plats")
@CrossOrigin(origins = "*")
public class PublicPlatController {

    private static final Logger logger = LoggerFactory.getLogger(PublicPlatController.class);

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private PromotionPlatRepository promotionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Autowired
    private PlatReviewRepository platReviewRepository;

    @Autowired
    private ChefReviewRepository chefReviewRepository;

    /**
     * Get all available plats with active promotions
     * Public endpoint - no authentication required
     * Optional location filtering within 30km radius
     * 
     * @param latitude user's current latitude (optional)
     * @param longitude user's current longitude (optional)
     * @param radiusKm maximum distance in kilometers (default: 30)
     * @return list of plats that have active promotions
     */
    @GetMapping("/promotions")
    public ResponseEntity<ApiResponse<List<PublicPlatResponse>>> getPlatsWithPromotions(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm) {
        try {
            logger.info("Public request to get plats with promotions (location: {}, {}, radius: {}km)", 
                latitude, longitude, radiusKm);

            // Get all available plats
            List<Plat> availablePlats = platRepository.findByAvailableTrue();

            // Filter plats that have active promotions and map to response
            List<PublicPlatResponse> platsWithPromotions = availablePlats.stream()
                .map(plat -> {
                    // Get active promotion
                    Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                        plat.getId(),
                        LocalDateTime.now()
                    );

                    // Only include plats with active promotions
                    if (promotion.isPresent()) {
                        PublicPlatResponse response = mapToPublicResponse(plat, promotion.get());
                        
                        // Add distance if location provided
                        if (latitude != null && longitude != null) {
                            addDistanceToResponse(response, latitude, longitude);
                        }
                        
                        return response;
                    }
                    return null;
                })
                .filter(response -> response != null) // Remove nulls (plats without promotions)
                .filter(response -> {
                    // Apply location filter if coordinates provided
                    if (latitude != null && longitude != null) {
                        if (response.getDistanceKm() == null) {
                            logger.warn("⚠️ Skipping plat {} - geocoding failed", response.getId());
                            return false; // Exclude if we couldn't calculate distance
                        }
                        return response.getDistanceKm() <= radiusKm;
                    }
                    return true; // No location filter requested
                })
                .collect(Collectors.toList());

            logger.info("Found {} plats with active promotions", platsWithPromotions.size());
            return ResponseEntity.ok(ApiResponse.success("Plats avec promotions récupérés avec succès", platsWithPromotions));

        } catch (Exception e) {
            logger.error("Error getting plats with promotions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la récupération des plats avec promotions"));
        }
    }

    /**
     * Get paginated plats with active promotions
     * Public endpoint - no authentication required
     * Optional location filtering within specified radius
     * Supports pagination with 10 items per page
     * 
     * @param latitude user's current latitude (optional)
     * @param longitude user's current longitude (optional)
     * @param radiusKm maximum distance in kilometers (default: 30)
     * @param page page number (default: 1)
     * @param pageSize items per page (default: 10)
     * @return paginated list of plats with active promotions
     */
    @GetMapping("/promotions/paginated")
    public ResponseEntity<ApiResponse<PaginatedResponse<PublicPlatResponse>>> getPlatsWithPromotionsPaginated(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        try {
            logger.info("Public request to get paginated plats with promotions (location: {}, {}, radius: {}km, page: {}, pageSize: {})", 
                latitude, longitude, radiusKm, page, pageSize);

            // Get all available plats
            List<Plat> availablePlats = platRepository.findByAvailableTrue();
            logger.info("Found {} available plats in database", availablePlats.size());

            // Filter plats that have active promotions and map to response
            List<PublicPlatResponse> platsWithPromotions = availablePlats.stream()
                .map(plat -> {
                    // Get active promotion
                    Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                        plat.getId(),
                        LocalDateTime.now()
                    );

                    // Only include plats with active promotions
                    if (promotion.isPresent()) {
                        PublicPlatResponse response = mapToPublicResponse(plat, promotion.get());
                        
                        // Add distance if location provided
                        if (latitude != null && longitude != null) {
                            addDistanceToResponse(response, latitude, longitude);
                        }
                        
                        return response;
                    }
                    return null;
                })
                .filter(response -> response != null) // Remove nulls (plats without promotions)
                .filter(response -> {
                    // Apply location filter if coordinates provided
                    if (latitude != null && longitude != null) {
                        if (response.getDistanceKm() == null) {
                            logger.warn("⚠️ Skipping promotional plat {} - geocoding failed", response.getId());
                            return false; // Exclude if we couldn't calculate distance
                        }
                        boolean withinRadius = response.getDistanceKm() <= radiusKm;
                        if (!withinRadius) {
                            logger.debug("Filtering out promotional plat '{}' - distance {}km exceeds radius {}km", 
                                response.getName(), response.getDistanceKm(), radiusKm);
                        }
                        return withinRadius;
                    }
                    return true; // No location filter requested
                })
                .collect(Collectors.toList());

            logger.info("After filtering: {} plats with active promotions", platsWithPromotions.size());

            // Sort by newest promotions first
            platsWithPromotions.sort((a, b) -> {
                LocalDateTime dateA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                LocalDateTime dateB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                return dateB.compareTo(dateA); // Descending order (newest first)
            });

            // Apply pagination
            PaginatedResponse<PublicPlatResponse> paginatedResponse = PaginatedResponse.of(platsWithPromotions, page, pageSize);

            logger.info("Paginated promotions returned {} total results, page {}/{}", 
                paginatedResponse.getPagination().getTotalItems(),
                paginatedResponse.getPagination().getCurrentPage(),
                paginatedResponse.getPagination().getTotalPages());
            
            return ResponseEntity.ok(ApiResponse.success("Plats avec promotions récupérés avec succès", paginatedResponse));

        } catch (Exception e) {
            logger.error("Error getting paginated plats with promotions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la récupération des plats avec promotions"));
        }
    }

    /**
     * Get all available plats (with or without promotions)
     * Public endpoint - no authentication required
     * Optional location filtering within 30km radius
     * Supports pagination with 10 items per page
     * 
     * @param latitude user's current latitude (optional)
     * @param longitude user's current longitude (optional)
     * @param radiusKm maximum distance in kilometers (default: 30)
     * @param page page number (1-based, default: 1)
     * @param pageSize items per page (default: 10)
     * @return paginated list of all available plats
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<PublicPlatResponse>>> getAllAvailablePlats(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        try {
            logger.info("Public request to get all available plats (location: {}, {}, radius: {}km, page: {}, pageSize: {})", 
                latitude, longitude, radiusKm, page, pageSize);

            // Get all available plats
            List<Plat> availablePlats = platRepository.findByAvailableTrue();

            // Map to response with promotions if they exist
            List<PublicPlatResponse> publicPlats = availablePlats.stream()
                .map(plat -> {
                    // Get active promotion if exists
                    Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                        plat.getId(),
                        LocalDateTime.now()
                    );

                    PublicPlatResponse response = mapToPublicResponse(plat, promotion.orElse(null));
                    
                    // Add distance if location provided
                    if (latitude != null && longitude != null) {
                        addDistanceToResponse(response, latitude, longitude);
                    }
                    
                    return response;
                })
                .filter(response -> {
                    // Apply location filter if coordinates provided
                    if (latitude != null && longitude != null) {
                        if (response.getDistanceKm() == null) {
                            logger.warn("⚠️ Skipping plat {} - geocoding failed", response.getId());
                            return false; // Exclude if we couldn't calculate distance
                        }
                        return response.getDistanceKm() <= radiusKm;
                    }
                    return true; // No location filter requested
                })
                .collect(Collectors.toList());

            // Apply pagination
            PaginatedResponse<PublicPlatResponse> paginatedResponse = PaginatedResponse.of(publicPlats, page, pageSize);

            logger.info("Found {} available plats, page {}/{}", 
                paginatedResponse.getPagination().getTotalItems(),
                paginatedResponse.getPagination().getCurrentPage(),
                paginatedResponse.getPagination().getTotalPages());
            return ResponseEntity.ok(ApiResponse.success("Plats disponibles récupérés avec succès", paginatedResponse));

        } catch (Exception e) {
            logger.error("Error getting available plats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la récupération des plats disponibles"));
        }
    }

    /**
     * Get all unique categories from plats within specified radius
     * Public endpoint - no authentication required
     * Filters by location to show only categories from nearby chefs
     * 
     * @param latitude user's current latitude (optional)
     * @param longitude user's current longitude (optional)
     * @param radiusKm maximum distance in kilometers (default: 30)
     * @return list of unique categories from nearby plats
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm) {
        try {
            logger.info("Public request to get categories (location: {}, {}, radius: {}km)", 
                latitude, longitude, radiusKm);

            // Get all available plats
            List<Plat> availablePlats = platRepository.findByAvailableTrue();
            logger.info("Found {} available plats before filtering", availablePlats.size());

            // Filter by location if provided
            List<Plat> filteredPlats = availablePlats;
            if (latitude != null && longitude != null) {
                filteredPlats = availablePlats.stream()
                    .filter(plat -> {
                        // Get chef user
                        Optional<User> chefOpt = userRepository.findById(plat.getChefId());
                        if (chefOpt.isEmpty() || chefOpt.get().getAddress() == null) {
                            return false;
                        }
                        
                        User chef = chefOpt.get();
                        Double[] chefCoords = GeocodingUtil.geocodeAddress(chef.getAddress());
                        if (chefCoords == null) {
                            return false;
                        }
                        
                        double distance = GeocodingUtil.calculateDistance(
                            latitude, longitude,
                            chefCoords[0], chefCoords[1]
                        );
                        
                        return distance <= radiusKm;
                    })
                    .collect(Collectors.toList());
                logger.info("After location filtering: {} plats within {}km", filteredPlats.size(), radiusKm);
            }

            // Extract all categories and remove duplicates
            List<String> uniqueCategories = filteredPlats.stream()
                .map(Plat::getCategories)
                .filter(categories -> categories != null && !categories.isEmpty())
                .flatMap(categories -> categories.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            logger.info("Found {} unique categories from filtered plats", uniqueCategories.size());
            return ResponseEntity.ok(ApiResponse.success("Catégories récupérées avec succès", uniqueCategories));

        } catch (Exception e) {
            logger.error("Error getting categories: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la récupération des catégories"));
        }
    }

    /**
     * Search plats by query string
     * Searches in name, description, categories, and chef name
     * Public endpoint - no authentication required
     * Optional location filtering within 30km radius
     * Supports pagination with 10 items per page
     * 
     * @param query the search query
     * @param category optional category filter
     * @param maxCookTime optional maximum cooking time filter
     * @param minRating optional minimum rating filter
     * @param sortBy optional sort field (rating, price, cookTime, newest)
     * @param sortOrder optional sort order (asc, desc)
     * @param latitude user's current latitude (optional)
     * @param longitude user's current longitude (optional)
     * @param radiusKm maximum distance in kilometers (default: 30)
     * @param page page number (1-based, default: 1)
     * @param pageSize items per page (default: 10)
     * @return paginated list of matching plats
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginatedResponse<PublicPlatResponse>>> searchPlats(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxCookTime,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {
        try {
            logger.info("Public search request - query: {}, category: {}, maxCookTime: {}, minRating: {}, sortBy: {}, sortOrder: {}, location: {}, {}, radius: {}km, page: {}, pageSize: {}", 
                query, category, maxCookTime, minRating, sortBy, sortOrder, latitude, longitude, radiusKm, page, pageSize);

            // Get all available plats
            List<Plat> availablePlats = platRepository.findByAvailableTrue();
            logger.info("Found {} available plats in database", availablePlats.size());

            // Apply filters
            List<PublicPlatResponse> filteredPlats = availablePlats.stream()
                .map(plat -> {
                    // Get active promotion if exists
                    Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                        plat.getId(),
                        LocalDateTime.now()
                    );
                    PublicPlatResponse response = mapToPublicResponse(plat, promotion.orElse(null));
                    
                    // Add distance if location provided
                    if (latitude != null && longitude != null) {
                        addDistanceToResponse(response, latitude, longitude);
                    }
                    
                    return response;
                })
                // Filter by search query (name, description, chef name)
                .filter(response -> {
                    if (query == null || query.trim().isEmpty()) {
                        return true;
                    }
                    String searchLower = query.toLowerCase().trim();
                    
                    // Search in name
                    boolean matchesName = response.getName() != null && 
                        response.getName().toLowerCase().contains(searchLower);
                    
                    // Search in description
                    boolean matchesDesc = response.getDescription() != null && 
                        response.getDescription().toLowerCase().contains(searchLower);
                    
                    // Search in chef name
                    boolean matchesChef = response.getChef() != null && (
                        (response.getChef().getFirstName() != null && 
                            response.getChef().getFirstName().toLowerCase().contains(searchLower)) ||
                        (response.getChef().getLastName() != null && 
                            response.getChef().getLastName().toLowerCase().contains(searchLower))
                    );
                    
                    // Search in categories
                    boolean matchesCategory = response.getCategories() != null && 
                        response.getCategories().stream()
                            .anyMatch(cat -> cat.toLowerCase().contains(searchLower));
                    
                    return matchesName || matchesDesc || matchesChef || matchesCategory;
                })
                // Filter by category
                .filter(response -> {
                    if (category == null || category.trim().isEmpty()) {
                        return true;
                    }
                    return response.getCategories() != null && 
                        response.getCategories().stream()
                            .anyMatch(cat -> cat.equalsIgnoreCase(category.trim()));
                })
                // Filter by max cooking time
                .filter(response -> {
                    if (maxCookTime == null) {
                        return true;
                    }
                    return response.getEstimatedCookTime() != null && 
                        response.getEstimatedCookTime() <= maxCookTime;
                })
                // Filter by minimum rating
                .filter(response -> {
                    if (minRating == null) {
                        return true;
                    }
                    return response.getAverageRating() != null && 
                        response.getAverageRating() >= minRating;
                })
                // Filter by location (radius)
                .filter(response -> {
                    if (latitude != null && longitude != null) {
                        if (response.getDistanceKm() == null) {
                            logger.warn("⚠️ Skipping plat {} - geocoding failed", response.getId());
                            return false; // Exclude if we couldn't calculate distance
                        }
                        boolean withinRadius = response.getDistanceKm() <= radiusKm;
                        if (!withinRadius) {
                            logger.debug("Filtering out plat '{}' - distance {}km exceeds radius {}km", 
                                response.getName(), response.getDistanceKm(), radiusKm);
                        }
                        return withinRadius;
                    }
                    return true; // No location filter requested
                })
                .collect(Collectors.toList());
            
            logger.info("After all filters: {} plats remaining", filteredPlats.size());

            // Sort results
            boolean ascending = "asc".equalsIgnoreCase(sortOrder);
            switch (sortBy.toLowerCase()) {
                case "rating":
                    filteredPlats.sort((a, b) -> {
                        Double ratingA = a.getAverageRating() != null ? a.getAverageRating() : 0.0;
                        Double ratingB = b.getAverageRating() != null ? b.getAverageRating() : 0.0;
                        return ascending ? ratingA.compareTo(ratingB) : ratingB.compareTo(ratingA);
                    });
                    break;
                case "price":
                    filteredPlats.sort((a, b) -> {
                        Double priceA = a.getPrice() != null ? a.getPrice() : 0.0;
                        Double priceB = b.getPrice() != null ? b.getPrice() : 0.0;
                        return ascending ? priceA.compareTo(priceB) : priceB.compareTo(priceA);
                    });
                    break;
                case "cooktime":
                    filteredPlats.sort((a, b) -> {
                        Integer timeA = a.getEstimatedCookTime() != null ? a.getEstimatedCookTime() : 0;
                        Integer timeB = b.getEstimatedCookTime() != null ? b.getEstimatedCookTime() : 0;
                        return ascending ? timeA.compareTo(timeB) : timeB.compareTo(timeA);
                    });
                    break;
                case "newest":
                default:
                    filteredPlats.sort((a, b) -> {
                        LocalDateTime dateA = a.getCreatedAt() != null ? a.getCreatedAt() : LocalDateTime.MIN;
                        LocalDateTime dateB = b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.MIN;
                        return ascending ? dateA.compareTo(dateB) : dateB.compareTo(dateA);
                    });
                    break;
            }

            // Apply pagination
            PaginatedResponse<PublicPlatResponse> paginatedResponse = PaginatedResponse.of(filteredPlats, page, pageSize);

            logger.info("Search returned {} total results, page {}/{}", 
                paginatedResponse.getPagination().getTotalItems(),
                paginatedResponse.getPagination().getCurrentPage(),
                paginatedResponse.getPagination().getTotalPages());
            return ResponseEntity.ok(ApiResponse.success("Recherche effectuée avec succès", paginatedResponse));

        } catch (Exception e) {
            logger.error("Error searching plats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la recherche des plats"));
        }
    }

    /**
     * Get a single plat by ID (if available)
     * Public endpoint - no authentication required
     * 
     * @param platId the plat ID
     * @return the plat details with chef information
     */
    @GetMapping("/{platId}")
    public ResponseEntity<ApiResponse<PublicPlatResponse>> getPlatById(@PathVariable UUID platId) {
        try {
            logger.info("Public request to get plat: {}", platId);

            // Get plat if available
            Optional<Plat> platOpt = platRepository.findByIdAndAvailableTrue(platId);

            if (platOpt.isEmpty()) {
                logger.warn("Plat not found or not available: {}", platId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Plat non trouvé ou non disponible"));
            }

            Plat plat = platOpt.get();

            // Get active promotion if exists
            Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                plat.getId(),
                LocalDateTime.now()
            );

            PublicPlatResponse response = mapToPublicResponse(plat, promotion.orElse(null));

            logger.info("Successfully retrieved plat: {}", platId);
            return ResponseEntity.ok(ApiResponse.success("Plat récupéré avec succès", response));

        } catch (Exception e) {
            logger.error("Error getting plat {}: {}", platId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la récupération du plat"));
        }
    }

    /**
     * Helper method to map Plat to PublicPlatResponse with chef info
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
        Double averageRating = calculateAverageRating(plat.getId());
        response.setAverageRating(averageRating);

        // Add chef information
        PublicPlatResponse.ChefInfo chefInfo = getChefInfo(plat.getChefId());
        response.setChef(chefInfo);
        
        // Add chef isOpen status
        Boolean isChefOpen = getChefOpenStatus(plat.getChefId());
        response.setIsChefOpen(isChefOpen);

        return response;
    }

    /**
     * Helper method to calculate average rating for a plat
     * @param platId the plat ID
     * @return average rating or 0.0 if no reviews exist
     */
    private Double calculateAverageRating(UUID platId) {
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
     * Helper method to calculate average rating for a chef
     * @param chefId the chef ID
     * @return average rating or 0.0 if no reviews exist
     */
    private Double calculateChefAverageRating(UUID chefId) {
        List<ChefReview> reviews = chefReviewRepository.findByChefId(chefId);
        
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = reviews.stream()
            .mapToInt(ChefReview::getRate)
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
            Double chefAverageRating = calculateChefAverageRating(chefId);

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
    
    /**
     * Helper method to get chef open/closed status
     */
    private Boolean getChefOpenStatus(UUID chefId) {
        Optional<ChefDescription> chefDescOpt = chefDescriptionRepository.findByUserId(chefId);
        return chefDescOpt.map(ChefDescription::getIsOpen).orElse(true); // Default to true if no description
    }

    /**
     * Add distance from user location to plate response
     * Calculates distance to chef's location and sets it in the response
     */
    private void addDistanceToResponse(PublicPlatResponse plat, Double userLat, Double userLon) {
        try {
            logger.info("🔍 addDistanceToResponse called for plat: {}", plat.getName());
            
            // Get chef's coordinates from their address
            Optional<User> chefOpt = userRepository.findById(plat.getChef().getId());
            if (chefOpt.isEmpty() || chefOpt.get().getAddress() == null) {
                logger.warn("⚠️ No address for chef {} - cannot calculate distance", plat.getChef().getId());
                return; // No address, distance remains null
            }

            String chefAddress = chefOpt.get().getAddress();
            logger.info("📍 Geocoding address for chef {}: {}", plat.getChef().getId(), chefAddress);
            Double[] chefCoords = GeocodingUtil.geocodeAddress(chefAddress);
            
            if (chefCoords == null) {
                logger.error("❌ Could not geocode address: {}", chefAddress);
                return; // Could not geocode address
            }

            logger.info("✅ Geocoded to: [{}, {}]", chefCoords[0], chefCoords[1]);
            
            // Calculate distance and set it in the response
            double distance = GeocodingUtil.calculateDistance(userLat, userLon, chefCoords[0], chefCoords[1]);
            plat.setDistanceKm(Math.round(distance * 10.0) / 10.0); // Round to 1 decimal place
            logger.info("✅ Distance calculated for plat {}: {} km", plat.getName(), plat.getDistanceKm());
            
        } catch (Exception e) {
            logger.error("❌ Error calculating distance for plat {}: {}", plat.getName(), e.getMessage(), e);
        }
    }

    /**
     * Check if a plat's chef is within the specified radius from user's location
     * Uses Haversine formula to calculate distance
     */
    private boolean isWithinRadius(PublicPlatResponse plat, Double userLat, Double userLon, Double radiusKm) {
        try {
            // Get chef's coordinates from their address
            Optional<User> chefOpt = userRepository.findById(plat.getChef().getId());
            if (chefOpt.isEmpty() || chefOpt.get().getAddress() == null) {
                return false; // No address, exclude from results
            }

            String chefAddress = chefOpt.get().getAddress();
            Double[] chefCoords = GeocodingUtil.geocodeAddress(chefAddress);
            
            if (chefCoords == null) {
                return false; // Could not geocode address
            }

            // Calculate distance
            double distance = GeocodingUtil.calculateDistance(userLat, userLon, chefCoords[0], chefCoords[1]);
            
            return distance <= radiusKm;
        } catch (Exception e) {
            logger.error("Error checking location radius: {}", e.getMessage());
            return false;
        }
    }

}
