package com.dineo_backend.dineo.chefs.controller;

import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.chefs.dto.ChefDashboardStatsResponse;
import com.dineo_backend.dineo.chefs.dto.ChefStatisticsResponse;
import com.dineo_backend.dineo.chefs.service.ChefStatisticsService;
import com.dineo_backend.dineo.plats.dto.ChefPlatsStatsResponse;
import com.dineo_backend.dineo.plats.dto.PlatStatisticsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for chef statistics endpoints
 * Provides real statistics data for chef dashboard, statistics page, and plats page
 */
@RestController
@RequestMapping("/api/v1/chefs/statistics")
public class ChefStatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(ChefStatisticsController.class);

    @Autowired
    private ChefStatisticsService chefStatisticsService;

    @Autowired
    private JwtService jwtService;

    /**
     * Get dashboard statistics for the authenticated chef
     * Used in chef-dashboard.tsx
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID chefId = extractChefId(authHeader);
            logger.info("Getting dashboard stats for chef: {}", chefId);

            ChefDashboardStatsResponse stats = chefStatisticsService.getDashboardStats(chefId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Dashboard stats retrieved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting dashboard stats: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve dashboard stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get comprehensive statistics for the authenticated chef
     * Used in chef-statistics.tsx
     * @param timeRange optional: day, week, month (default), year
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false, defaultValue = "month") String timeRange) {
        try {
            UUID chefId = extractChefId(authHeader);
            logger.info("Getting statistics for chef: {} with timeRange: {}", chefId, timeRange);

            ChefStatisticsResponse stats = chefStatisticsService.getStatistics(chefId, timeRange);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Statistics retrieved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting statistics: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get plats with statistics for the authenticated chef
     * Used in chef-plates.tsx
     */
    @GetMapping("/plats")
    public ResponseEntity<Map<String, Object>> getPlatsStats(
            @RequestHeader("Authorization") String authHeader) {
        try {
            UUID chefId = extractChefId(authHeader);
            logger.info("Getting plats stats for chef: {}", chefId);

            ChefPlatsStatsResponse stats = chefStatisticsService.getPlatsStats(chefId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Plats stats retrieved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting plats stats: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve plats stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get statistics for a specific plat
     * Used in plate-statistics.tsx
     */
    @GetMapping("/plats/{platId}")
    public ResponseEntity<Map<String, Object>> getPlatStatistics(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID platId) {
        try {
            UUID chefId = extractChefId(authHeader);
            logger.info("Getting statistics for plat: {} by chef: {}", platId, chefId);

            PlatStatisticsResponse stats = chefStatisticsService.getPlatStatistics(platId, chefId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "Plat statistics retrieved successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting plat statistics: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve plat statistics: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Extract chef ID from JWT token
     */
    private UUID extractChefId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        String userId = jwtService.extractUsername(token);
        return UUID.fromString(userId);
    }
}
