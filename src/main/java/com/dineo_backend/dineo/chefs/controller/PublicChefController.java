package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.authentication.enums.Role;
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
import com.dineo_backend.dineo.common.PaginatedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Public REST Controller for Chef operations
 * These endpoints don't require authentication and are used for public chef information
 */
@RestController
@RequestMapping("/api/v1/public/chefs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PublicChefController {

    private static final Logger logger = LoggerFactory.getLogger(PublicChefController.class);
    
    // Cache for geocoded addresses to reduce API calls
    private static final Map<String, double[]> geocodeCache = new ConcurrentHashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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
    
    /**
     * Get nearby chefs within a specified radius
     * Public endpoint - no authentication required
     * 
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param radiusKm Radius in kilometers (default: 30)
     * @param page Page number (0-indexed, default: 0)
     * @param pageSize Page size (default: 10)
     * @return Paginated list of chefs within radius, sorted by distance
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<PaginatedResponse<PublicChefResponse>>> getNearbyChefs(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "30") Double radiusKm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("Get nearby chefs request received for location ({}, {}) within {} km", 
                       latitude, longitude, radiusKm);

            // Get all chefs (users with PROVIDER role)
            List<UserRole> providerRoles = roleRepository.findByRole(Role.PROVIDER);
            
            if (providerRoles.isEmpty()) {
                PaginatedResponse<PublicChefResponse> paginatedResponse = new PaginatedResponse<>();
                paginatedResponse.setItems(new ArrayList<>());
                paginatedResponse.setPagination(new PaginatedResponse.PaginationMetadata(
                    page + 1, pageSize, 0, 0, false, false
                ));
                
                ApiResponse<PaginatedResponse<PublicChefResponse>> apiResponse = ApiResponse.success(
                    "Aucun chef trouvé",
                    paginatedResponse
                );
                return ResponseEntity.ok(apiResponse);
            }
            
            // Get user IDs from provider roles
            List<UUID> chefIds = providerRoles.stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
            
            logger.info("Found {} PROVIDER users", chefIds.size());
            
            // Get all chef users
            List<User> allChefs = userRepository.findAllById(chefIds);
            
            logger.info("Retrieved {} chef users from database", allChefs.size());

            // Map chefs to responses and add distances
            List<PublicChefResponse> allChefResponses = allChefs.stream()
                .map(chef -> {
                    try {
                        // Get chef description
                        Optional<ChefDescription> chefDescOpt = chefDescriptionRepository.findByUserId(chef.getId());
                        
                        // Calculate chef average rating and total reviews
                        List<ChefReview> reviews = chefReviewRepository.findByChefId(chef.getId());
                        Double averageRating = calculateChefAverageRating(reviews);
                        Integer totalReviews = reviews.size();

                        // Create response
                        PublicChefResponse response = new PublicChefResponse();
                        response.setId(chef.getId());
                        response.setFirstName(chef.getFirstName());
                        response.setLastName(chef.getLastName());
                        response.setAddress(chef.getAddress());
                        response.setCreatedAt(chef.getCreatedAt());
                        response.setAverageRating(averageRating);
                        response.setTotalReviews(totalReviews);

                        if (chefDescOpt.isPresent()) {
                            ChefDescription chefDesc = chefDescOpt.get();
                            response.setChefCoverImg(chefDesc.getChefCoverImg());
                            response.setDescription(chefDesc.getDescription());
                            response.setCategories(chefDesc.getCategories());
                            response.setChefCertifications(chefDesc.getChefCertifications());
                        }
                        
                        // Add distance to response
                        addDistanceToChefResponse(response, latitude, longitude);
                        
                        return response;
                    } catch (Exception e) {
                        logger.error("Error processing chef {}: {}", chef.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(response -> response != null) // Remove nulls (chefs with errors)
                .filter(response -> {
                    // Apply location filter - only include chefs within radius
                    if (response.getDistanceKm() != null) {
                        return response.getDistanceKm() <= radiusKm;
                    }
                    return false; // No distance calculated, exclude
                })
                .sorted(Comparator.comparing(PublicChefResponse::getDistanceKm)) // Sort by distance (closest first)
                .collect(Collectors.toList());
            
            logger.info("Found {} chefs within {} km radius", allChefResponses.size(), radiusKm);
            
            // Calculate pagination
            int totalElements = allChefResponses.size();
            int totalPages = (int) Math.ceil((double) totalElements / pageSize);
            int startIndex = page * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalElements);
            
            // Get page content
            List<PublicChefResponse> pageContent = new ArrayList<>();
            if (startIndex < totalElements) {
                pageContent = allChefResponses.subList(startIndex, endIndex);
            }
            
            // Create paginated response
            PaginatedResponse<PublicChefResponse> paginatedResponse = new PaginatedResponse<>();
            paginatedResponse.setItems(pageContent);
            paginatedResponse.setPagination(new PaginatedResponse.PaginationMetadata(
                page + 1, // Convert to 1-based
                pageSize,
                totalElements,
                totalPages,
                page + 1 < totalPages,
                page > 0
            ));

            ApiResponse<PaginatedResponse<PublicChefResponse>> apiResponse = ApiResponse.success(
                String.format("%d chef(s) trouvé(s) dans un rayon de %.1f km", totalElements, radiusKm),
                paginatedResponse
            );

            logger.info("Successfully retrieved {} nearby chefs (page {}/{})", 
                       pageContent.size(), page + 1, totalPages);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            logger.error("Unexpected error getting nearby chefs", e);
            ApiResponse<PaginatedResponse<PublicChefResponse>> apiResponse = ApiResponse.error(
                "Erreur lors de la récupération des chefs à proximité"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }
    
    /**
     * Geocode an address to latitude/longitude using Nominatim
     */
    private double[] geocodeAddress(String address) {
        // Check cache first
        if (geocodeCache.containsKey(address)) {
            return geocodeCache.get(address);
        }
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = String.format(
                "https://nominatim.openstreetmap.org/search?format=json&q=%s&limit=1",
                java.net.URLEncoder.encode(address, "UTF-8")
            );
            
            // Add User-Agent header (required by Nominatim)
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "DineoApp/1.0");
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                List.class
            );
            
            List<Map<String, Object>> results = response.getBody();
            
            if (results != null && !results.isEmpty()) {
                Map<String, Object> firstResult = results.get(0);
                double lat = Double.parseDouble(firstResult.get("lat").toString());
                double lon = Double.parseDouble(firstResult.get("lon").toString());
                
                double[] coords = new double[]{lat, lon};
                
                // Cache the result
                geocodeCache.put(address, coords);
                
                return coords;
            }
        } catch (Exception e) {
            logger.error("Error geocoding address '{}': {}", address, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * Add distance from user location to chef response
     * Calculates distance to chef's location and sets it in the response
     */
    private void addDistanceToChefResponse(PublicChefResponse chef, Double userLat, Double userLon) {
        try {
            // Skip if no address
            if (chef.getAddress() == null || chef.getAddress().trim().isEmpty()) {
                logger.warn("Chef {} has no address - cannot calculate distance", chef.getId());
                return;
            }

            // Geocode chef's address
            double[] chefCoords = geocodeAddress(chef.getAddress());
            
            if (chefCoords == null) {
                logger.warn("Could not geocode address for chef {}: {}", chef.getId(), chef.getAddress());
                return;
            }
            
            // Calculate distance and set it in the response
            double distance = calculateDistance(userLat, userLon, chefCoords[0], chefCoords[1]);
            chef.setDistanceKm(Math.round(distance * 10.0) / 10.0); // Round to 1 decimal place
            logger.info("Distance calculated for chef {} ({}): {} km", 
                chef.getId(), chef.getFirstName() + " " + chef.getLastName(), chef.getDistanceKm());
            
        } catch (Exception e) {
            logger.error("Error calculating distance for chef {}: {}", chef.getId(), e.getMessage(), e);
        }
    }
}
