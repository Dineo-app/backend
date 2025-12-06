package com.dineo_backend.dineo.chefs.service;

import com.dineo_backend.dineo.chefs.dto.ChefDashboardStatsResponse;
import com.dineo_backend.dineo.chefs.dto.ChefStatisticsResponse;
import com.dineo_backend.dineo.plats.dto.ChefPlatsStatsResponse;
import com.dineo_backend.dineo.plats.dto.PlatStatisticsResponse;

import java.util.UUID;

/**
 * Service interface for chef statistics operations
 */
public interface ChefStatisticsService {

    /**
     * Get dashboard statistics for a chef
     * @param chefId the ID of the chef
     * @return dashboard statistics
     */
    ChefDashboardStatsResponse getDashboardStats(UUID chefId);

    /**
     * Get comprehensive statistics for a chef
     * @param chefId the ID of the chef
     * @param timeRange the time range (day, week, month, year)
     * @return comprehensive statistics
     */
    ChefStatisticsResponse getStatistics(UUID chefId, String timeRange);

    /**
     * Get statistics for all chef's plats
     * @param chefId the ID of the chef
     * @return plats with statistics
     */
    ChefPlatsStatsResponse getPlatsStats(UUID chefId);

    /**
     * Get statistics for a specific plat
     * @param platId the ID of the plat
     * @param chefId the ID of the chef (for validation)
     * @return plat statistics
     */
    PlatStatisticsResponse getPlatStatistics(UUID platId, UUID chefId);
}
