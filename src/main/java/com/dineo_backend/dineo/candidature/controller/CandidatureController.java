package com.dineo_backend.dineo.candidature.controller;

import com.dineo_backend.dineo.candidature.dto.CandidatureResponse;
import com.dineo_backend.dineo.candidature.dto.CreateCandidatureRequest;
import com.dineo_backend.dineo.candidature.service.CandidatureService;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Candidature operations
 * Public endpoint for creating candidatures
 * Admin endpoints for managing candidatures
 */
@RestController
@RequestMapping("/api/v1/candidatures")
public class CandidatureController {
    
    private static final Logger logger = LoggerFactory.getLogger(CandidatureController.class);
    
    private final CandidatureService candidatureService;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    
    public CandidatureController(CandidatureService candidatureService,
                                 JwtService jwtService,
                                 RoleRepository roleRepository) {
        this.candidatureService = candidatureService;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }
    
    /**
     * Create new candidature (PUBLIC - no authentication required)
     * 
     * @param candidatureData JSON string containing candidature data
     * @param resumeFile PDF file of the resume
     * @return created candidature
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CandidatureResponse>> createCandidature(
            @RequestParam("candidatureData") String candidatureData,
            @RequestParam("resume") MultipartFile resumeFile) {
        
        logger.info("Received public candidature submission");
        
        try {
            // Parse JSON candidature data
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules(); // For LocalDate support
            CreateCandidatureRequest request = objectMapper.readValue(candidatureData, CreateCandidatureRequest.class);
            
            // Validate resume file
            if (resumeFile == null || resumeFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Le CV est requis"));
            }
            
            // Create candidature
            CandidatureResponse candidature = candidatureService.createCandidature(request, resumeFile);
            
            logger.info("Candidature created successfully: {}", candidature.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Candidature soumise avec succès", candidature));
                
        } catch (RuntimeException e) {
            logger.error("Error creating candidature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating candidature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }
    
    /**
     * Get all candidatures with filters (ADMIN only)
     * 
     * @param isViewed filter by viewed status
     * @param startDate filter from date
     * @param endDate filter to date
     * @param country filter by country
     * @param page page number (0-indexed)
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDir sort direction (asc/desc)
     * @param authHeader authorization header
     * @return page of candidatures
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCandidatures(
            @RequestParam(required = false) Boolean isViewed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Received request to get all candidatures");
        
        try {
            // Extract JWT token and verify admin role
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);
            
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to access candidatures without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                          "Accès refusé. Seuls les administrateurs peuvent voir les candidatures."));
            }
            
            // Create pagination and sorting
            Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get candidatures
            Page<CandidatureResponse> candidatures = candidatureService.getAllCandidatures(
                isViewed, startDate, endDate, country, pageable);
            
            // Get unviewed count
            long unviewedCount = candidatureService.getUnviewedCount();
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("candidatures", candidatures.getContent());
            response.put("currentPage", candidatures.getNumber());
            response.put("totalItems", candidatures.getTotalElements());
            response.put("totalPages", candidatures.getTotalPages());
            response.put("unviewedCount", unviewedCount);
            
            logger.info("Retrieved {} candidatures for admin: {}", candidatures.getTotalElements(), adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Candidatures récupérées avec succès", response));
            
        } catch (RuntimeException e) {
            logger.error("Error getting candidatures: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting candidatures: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }
    
    /**
     * Get candidature by ID (ADMIN only) - marks as viewed
     * 
     * @param id candidature ID
     * @param authHeader authorization header
     * @return candidature details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidatureResponse>> getCandidatureById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {
        
        logger.info("Received request to get candidature: {}", id);
        
        try {
            // Extract JWT token and verify admin role
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);
            
            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to access candidature without Administrator role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                          "Accès refusé. Seuls les administrateurs peuvent voir les candidatures."));
            }
            
            // Get candidature (will mark as viewed)
            CandidatureResponse candidature = candidatureService.getCandidatureById(id);
            
            logger.info("Retrieved candidature {} for admin: {}", id, adminUserId);
            
            return ResponseEntity.ok(ApiResponse.success("Candidature récupérée avec succès", candidature));
            
        } catch (RuntimeException e) {
            logger.error("Error getting candidature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting candidature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }
    
    /**
     * Get count of unviewed candidatures (ADMIN only)
     * 
     * @param authHeader authorization header
     * @return count of unviewed candidatures
     */
    @GetMapping("/unviewed-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnviewedCount(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Extract JWT token and verify admin role
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);
            
            if (!hasAdministratorRole(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), 
                          "Accès refusé."));
            }
            
            long count = candidatureService.getUnviewedCount();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            
            return ResponseEntity.ok(ApiResponse.success("Nombre de candidatures non vues", response));
            
        } catch (Exception e) {
            logger.error("Error getting unviewed count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }
    
    /**
     * Check if user has Administrator role
     */
    private boolean hasAdministratorRole(UUID userId) {
        return roleRepository.findByUserIdAndRole(userId, Role.ADMIN).isPresent();
    }
}
