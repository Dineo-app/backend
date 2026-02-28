package com.dineo_backend.dineo.contact.controller;

import com.dineo_backend.dineo.contact.dto.ContactMessageResponse;
import com.dineo_backend.dineo.contact.dto.CreateContactRequest;
import com.dineo_backend.dineo.contact.service.ContactMessageService;
import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final ContactMessageService contactMessageService;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    public ContactController(ContactMessageService contactMessageService,
                             JwtService jwtService,
                             RoleRepository roleRepository) {
        this.contactMessageService = contactMessageService;
        this.jwtService = jwtService;
        this.roleRepository = roleRepository;
    }

    /** Create new contact message (PUBLIC - no authentication required) */
    @PostMapping
    public ResponseEntity<ApiResponse<ContactMessageResponse>> createMessage(
            @Valid @RequestBody CreateContactRequest request) {

        logger.info("Received public contact form submission from: {}", request.getEmail());
        try {
            ContactMessageResponse message = contactMessageService.createMessage(request);
            logger.info("Contact message created: {}", message.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Message envoyé avec succès", message));
        } catch (RuntimeException e) {
            logger.error("Error creating contact message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating contact message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /** Get all contact messages with filters (ADMIN only) */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllMessages(
            @RequestParam(required = false) Boolean isViewed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Received request to get all contact messages");
        try {
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);

            if (!hasAdministratorRole(adminUserId)) {
                logger.warn("User {} attempted to access contact messages without Admin role", adminUserId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(),
                                "Accès refusé. Seuls les administrateurs peuvent voir les messages."));
            }

            Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ContactMessageResponse> messages = contactMessageService.getAllMessages(
                    isViewed, startDate, endDate, pageable);
            long unviewedCount = contactMessageService.getUnviewedCount();

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalItems", messages.getTotalElements());
            response.put("totalPages", messages.getTotalPages());
            response.put("unviewedCount", unviewedCount);

            logger.info("Retrieved {} contact messages for admin: {}", messages.getTotalElements(), adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Messages récupérés avec succès", response));
        } catch (RuntimeException e) {
            logger.error("Error getting contact messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting contact messages: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /** Get contact message by ID (ADMIN only) - marks as viewed */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> getMessageById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        logger.info("Received request to get contact message: {}", id);
        try {
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);

            if (!hasAdministratorRole(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Accès refusé."));
            }

            ContactMessageResponse message = contactMessageService.getMessageById(id);
            logger.info("Retrieved contact message {} for admin: {}", id, adminUserId);
            return ResponseEntity.ok(ApiResponse.success("Message récupéré avec succès", message));
        } catch (RuntimeException e) {
            logger.error("Error getting contact message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error getting contact message: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /** Get count of unviewed messages (ADMIN only) */
    @GetMapping("/unviewed-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnviewedCount(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            UUID adminUserId = jwtService.extractUserId(token);

            if (!hasAdministratorRole(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), "Accès refusé."));
            }

            long count = contactMessageService.getUnviewedCount();
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(ApiResponse.success("Nombre de messages non lus", response));
        } catch (Exception e) {
            logger.error("Error getting unviewed count: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    private boolean hasAdministratorRole(UUID userId) {
        return roleRepository.findByUserIdAndRole(userId, Role.ADMIN).isPresent();
    }
}
