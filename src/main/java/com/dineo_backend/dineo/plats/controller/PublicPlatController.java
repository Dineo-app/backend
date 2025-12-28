package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.model.ChefReview;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.chefs.repository.ChefReviewRepository;
import com.dineo_backend.dineo.plats.dto.PromotionResponse;
import com.dineo_backend.dineo.plats.dto.PublicPlatResponse;
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
                        return mapToPublicResponse(plat, promotion.get());
                    }
                    return null;
                })
                .filter(response -> response != null) // Remove nulls (plats without promotions)
                .filter(response -> {
                    // Apply location filter if coordinates provided
                    if (latitude != null && longitude != null) {
                        return isWithinRadius(response, latitude, longitude, radiusKm);
                    }
                    return true; // No location filter
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
     * Get all available plats (with or without promotions)
     * Public endpoint - no authentication required
     * Optional location filtering within 30km radius
     * 
     * @param latitude user's current latitude (optional)
     * @param longitude user's current longitude (optional)
     * @param radiusKm maximum distance in kilometers (default: 30)
     * @return list of all available plats
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PublicPlatResponse>>> getAllAvailablePlats(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm) {
        try {
            logger.info("Public request to get all available plats (location: {}, {}, radius: {}km)", 
                latitude, longitude, radiusKm);

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

                    return mapToPublicResponse(plat, promotion.orElse(null));
                })
                .filter(response -> {
                    // Apply location filter if coordinates provided
                    if (latitude != null && longitude != null) {
                        return isWithinRadius(response, latitude, longitude, radiusKm);
                    }
                    return true; // No location filter
                })
                .collect(Collectors.toList());

            logger.info("Found {} available plats", publicPlats.size());
            return ResponseEntity.ok(ApiResponse.success("Plats disponibles récupérés avec succès", publicPlats));

        } catch (Exception e) {
            logger.error("Error getting available plats: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "Erreur lors de la récupération des plats disponibles"));
        }
    }

    /**
     * Get all unique categories from all plats in the system
     * Public endpoint - no authentication required
     * 
     * @return list of unique categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        try {
            logger.info("Public request to get all categories");

            // Get all plats (not just available ones, to show all categories)
            List<Plat> allPlats = platRepository.findAll();

            // Extract all categories and remove duplicates
            List<String> uniqueCategories = allPlats.stream()
                .map(Plat::getCategories)
                .filter(categories -> categories != null && !categories.isEmpty())
                .flatMap(categories -> categories.stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

            logger.info("Found {} unique categories", uniqueCategories.size());
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
     * @return list of matching plats
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PublicPlatResponse>>> searchPlats(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxCookTime,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "30") Double radiusKm) {
        try {
            logger.info("Public search request - query: {}, category: {}, maxCookTime: {}, minRating: {}, sortBy: {}, sortOrder: {}, location: {}, {}, radius: {}km", 
                query, category, maxCookTime, minRating, sortBy, sortOrder, latitude, longitude, radiusKm);

            // Get all available plats
            List<Plat> availablePlats = platRepository.findByAvailableTrue();

            // Apply filters
            List<PublicPlatResponse> filteredPlats = availablePlats.stream()
                .map(plat -> {
                    // Get active promotion if exists
                    Optional<PromotionPlat> promotion = promotionRepository.findActivePromotionByPlatId(
                        plat.getId(),
                        LocalDateTime.now()
                    );
                    return mapToPublicResponse(plat, promotion.orElse(null));
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
                // Filter by location (30km radius)
                .filter(response -> {
                    if (latitude != null && longitude != null) {
                        return isWithinRadius(response, latitude, longitude, radiusKm);
                    }
                    return true; // No location filter
                })
                .collect(Collectors.toList());

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

            logger.info("Search returned {} results", filteredPlats.size());
            return ResponseEntity.ok(ApiResponse.success("Recherche effectuée avec succès", filteredPlats));

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
            Double[] chefCoords = geocodeAddress(chefAddress);
            
            if (chefCoords == null) {
                return false; // Could not geocode address
            }

            // Calculate distance
            double distance = calculateDistance(userLat, userLon, chefCoords[0], chefCoords[1]);
            
            return distance <= radiusKm;
        } catch (Exception e) {
            logger.error("Error checking location radius: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Geocode address to coordinates
     * Simplified Paris-focused geocoding based on postal code
     */
    private Double[] geocodeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        // Extract postal code (5 digits for France)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{5})");
        java.util.regex.Matcher matcher = pattern.matcher(address);
        
        if (matcher.find()) {
            String postalCode = matcher.group(1);
            
            // Paris arrondissements (750XX)
            if (postalCode.startsWith("750")) {
                int arrondissement = Integer.parseInt(postalCode.substring(3));
                return getParisArrondissementCoords(arrondissement);
            }
            
            // Other major French cities - you can expand this
            // For now, return approximate coordinates for common postal codes
        }
        
        return null;
    }

    /**
     * Get approximate coordinates for Paris arrondissements
     */
    private Double[] getParisArrondissementCoords(int arrondissement) {
        switch (arrondissement) {
            case 1: return new Double[]{48.8606, 2.3376};
            case 2: return new Double[]{48.8679, 2.3410};
            case 3: return new Double[]{48.8630, 2.3633};
            case 4: return new Double[]{48.8543, 2.3527};
            case 5: return new Double[]{48.8445, 2.3477};
            case 6: return new Double[]{48.8496, 2.3320};
            case 7: return new Double[]{48.8565, 2.3105};
            case 8: return new Double[]{48.8738, 2.3115};
            case 9: return new Double[]{48.8768, 2.3386};
            case 10: return new Double[]{48.8760, 2.3627};
            case 11: return new Double[]{48.8594, 2.3808};
            case 12: return new Double[]{48.8412, 2.3882};
            case 13: return new Double[]{48.8322, 2.3561};
            case 14: return new Double[]{48.8339, 2.3270};
            case 15: return new Double[]{48.8420, 2.2945};
            case 16: return new Double[]{48.8637, 2.2686};
            case 17: return new Double[]{48.8873, 2.3147};
            case 18: return new Double[]{48.8922, 2.3445};
            case 19: return new Double[]{48.8838, 2.3821};
            case 20: return new Double[]{48.8637, 2.3997};
            default: return null;
        }
    }

    /**
     * Calculate distance between two coordinates using Haversine formula
     * Returns distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
