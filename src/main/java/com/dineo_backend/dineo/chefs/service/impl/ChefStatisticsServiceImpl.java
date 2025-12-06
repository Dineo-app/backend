package com.dineo_backend.dineo.chefs.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.dto.ChefDashboardStatsResponse;
import com.dineo_backend.dineo.chefs.dto.ChefStatisticsResponse;
import com.dineo_backend.dineo.chefs.model.ChefReview;
import com.dineo_backend.dineo.chefs.repository.ChefReviewRepository;
import com.dineo_backend.dineo.chefs.service.ChefStatisticsService;
import com.dineo_backend.dineo.orders.enums.OrderStatus;
import com.dineo_backend.dineo.orders.model.Order;
import com.dineo_backend.dineo.orders.repository.OrderRepository;
import com.dineo_backend.dineo.plats.dto.ChefPlatsStatsResponse;
import com.dineo_backend.dineo.plats.dto.PlatStatisticsResponse;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.PlatReview;
import com.dineo_backend.dineo.plats.model.PromotionPlat;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.PlatReviewRepository;
import com.dineo_backend.dineo.plats.repository.PromotionPlatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ChefStatisticsService
 * Provides real statistics calculated from orders, reviews, and plats data
 */
@Service
public class ChefStatisticsServiceImpl implements ChefStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(ChefStatisticsServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private PlatReviewRepository platReviewRepository;

    @Autowired
    private ChefReviewRepository chefReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PromotionPlatRepository promotionPlatRepository;

    @Override
    @Transactional(readOnly = true)
    public ChefDashboardStatsResponse getDashboardStats(UUID chefId) {
        logger.info("Getting dashboard stats for chef: {}", chefId);

        ChefDashboardStatsResponse response = new ChefDashboardStatsResponse();

        // Calculate date ranges
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // Orders today
        long ordersToday = orderRepository.countByChefIdAndCreatedAtBetween(chefId, startOfToday, endOfToday);
        response.setOrdersToday((int) ordersToday);

        // Revenue today (completed orders)
        Double revenueToday = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                chefId, startOfToday, endOfToday, OrderStatus.COMPLETED);
        response.setRevenueToday(revenueToday != null ? revenueToday : 0.0);

        // Orders this month
        long ordersThisMonth = orderRepository.countByChefIdAndCreatedAtBetween(chefId, startOfMonth, now);
        response.setOrdersThisMonth((int) ordersThisMonth);

        // Revenue this month (completed orders)
        Double revenueThisMonth = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                chefId, startOfMonth, now, OrderStatus.COMPLETED);
        response.setRevenueThisMonth(revenueThisMonth != null ? revenueThisMonth : 0.0);

        // Revenue last month for comparison
        Double revenueLastMonth = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                chefId, startOfLastMonth, endOfLastMonth, OrderStatus.COMPLETED);
        String revenueChange = calculatePercentChange(revenueLastMonth, revenueThisMonth);
        response.setRevenueChangePercent(revenueChange);

        // Total plats
        List<Plat> chefPlats = platRepository.findByChefId(chefId);
        response.setTotalPlats(chefPlats.size());

        // New plats this month
        int newPlatsThisMonth = (int) chefPlats.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startOfMonth))
                .count();
        response.setNewPlatsThisMonth(newPlatsThisMonth);

        // Average rating from chef reviews
        double averageRating = calculateChefAverageRating(chefId);
        response.setAverageRating(Math.round(averageRating * 10.0) / 10.0);

        // Total reviews
        List<ChefReview> chefReviews = chefReviewRepository.findByChefId(chefId);
        response.setTotalReviews(chefReviews.size());

        // Rating percentage (positive reviews / total * 100)
        long positiveReviews = chefReviews.stream().filter(r -> r.getRate() >= 4).count();
        int ratingPercent = chefReviews.isEmpty() ? 0 : (int) ((positiveReviews * 100) / chefReviews.size());
        response.setRatingPercent(ratingPercent + "%");

        // Recent orders
        List<Order> recentOrders = orderRepository.findRecentOrdersByChefId(chefId, 5);
        List<ChefDashboardStatsResponse.RecentOrderSummary> recentOrderSummaries = recentOrders.stream()
                .map(order -> mapToRecentOrderSummary(order))
                .collect(Collectors.toList());
        response.setRecentOrders(recentOrderSummaries);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ChefStatisticsResponse getStatistics(UUID chefId, String timeRange) {
        logger.info("Getting comprehensive stats for chef: {} with timeRange: {}", chefId, timeRange);

        ChefStatisticsResponse response = new ChefStatisticsResponse();

        // Calculate date ranges based on timeRange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        LocalDateTime previousStartDate;
        LocalDateTime previousEndDate;

        switch (timeRange.toLowerCase()) {
            case "day":
                startDate = LocalDate.now().atStartOfDay();
                previousStartDate = startDate.minusDays(1);
                previousEndDate = startDate.minusSeconds(1);
                break;
            case "week":
                startDate = now.minusWeeks(1);
                previousStartDate = startDate.minusWeeks(1);
                previousEndDate = startDate.minusSeconds(1);
                break;
            case "year":
                startDate = now.minusYears(1);
                previousStartDate = startDate.minusYears(1);
                previousEndDate = startDate.minusSeconds(1);
                break;
            case "month":
            default:
                startDate = now.minusMonths(1);
                previousStartDate = startDate.minusMonths(1);
                previousEndDate = startDate.minusSeconds(1);
                break;
        }

        // Total revenue (all time completed orders)
        Double totalRevenue = orderRepository.sumTotalPriceByChefIdAndStatus(chefId, OrderStatus.COMPLETED);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // Total orders (all time)
        long totalOrders = orderRepository.countByChefId(chefId);
        response.setTotalOrders((int) totalOrders);

        // Average rating
        double averageRating = calculateChefAverageRating(chefId);
        response.setAverageRating(Math.round(averageRating * 10.0) / 10.0);

        // Profile views (placeholder - would need view tracking implementation)
        response.setProfileViews(0);

        // Calculate changes
        Double currentRevenue = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                chefId, startDate, now, OrderStatus.COMPLETED);
        Double previousRevenue = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                chefId, previousStartDate, previousEndDate, OrderStatus.COMPLETED);
        response.setRevenueChange(calculatePercentChange(previousRevenue, currentRevenue));

        long currentOrders = orderRepository.countByChefIdAndCreatedAtBetween(chefId, startDate, now);
        long previousOrders = orderRepository.countByChefIdAndCreatedAtBetween(chefId, previousStartDate, previousEndDate);
        response.setOrdersChange(calculatePercentChange((double) previousOrders, (double) currentOrders));

        response.setRatingChange("+0.0"); // Would need historical rating tracking
        response.setViewsChange("+0%"); // Would need view tracking

        // Generate time-based stats for charts based on timeRange
        List<ChefStatisticsResponse.MonthlyStats> periodStats = new ArrayList<>();
        
        switch (timeRange.toLowerCase()) {
            case "day":
                // Show hourly data for today (last 24 hours in 6 intervals)
                for (int i = 5; i >= 0; i--) {
                    LocalDateTime periodStart = now.minusHours(i * 4);
                    LocalDateTime periodEnd = periodStart.plusHours(4);
                    if (periodEnd.isAfter(now)) periodEnd = now;
                    
                    Double periodRevenue = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                            chefId, periodStart, periodEnd, OrderStatus.COMPLETED);
                    long periodOrders = orderRepository.countByChefIdAndCreatedAtBetweenAndStatus(
                            chefId, periodStart, periodEnd, OrderStatus.COMPLETED);
                    
                    String label = periodStart.format(DateTimeFormatter.ofPattern("HH:mm"));
                    
                    periodStats.add(new ChefStatisticsResponse.MonthlyStats(
                            label,
                            now.getYear(),
                            periodRevenue != null ? periodRevenue : 0.0,
                            (int) periodOrders,
                            averageRating
                    ));
                }
                break;
                
            case "week":
                // Show daily data for last 7 days
                for (int i = 6; i >= 0; i--) {
                    LocalDateTime dayStart = LocalDate.now().minusDays(i).atStartOfDay();
                    LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);
                    
                    Double dayRevenue = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                            chefId, dayStart, dayEnd, OrderStatus.COMPLETED);
                    long dayOrders = orderRepository.countByChefIdAndCreatedAtBetweenAndStatus(
                            chefId, dayStart, dayEnd, OrderStatus.COMPLETED);
                    
                    String dayName = dayStart.getDayOfWeek().toString().substring(0, 3);
                    dayName = dayName.charAt(0) + dayName.substring(1).toLowerCase();
                    
                    periodStats.add(new ChefStatisticsResponse.MonthlyStats(
                            dayName,
                            dayStart.getYear(),
                            dayRevenue != null ? dayRevenue : 0.0,
                            (int) dayOrders,
                            averageRating
                    ));
                }
                break;
                
            case "year":
                // Show monthly data for last 12 months
                for (int i = 11; i >= 0; i--) {
                    LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);

                    Double monthRevenue = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                            chefId, monthStart, monthEnd, OrderStatus.COMPLETED);
                    long monthOrders = orderRepository.countByChefIdAndCreatedAtBetweenAndStatus(
                            chefId, monthStart, monthEnd, OrderStatus.COMPLETED);

                    String monthName = monthStart.getMonth().toString().substring(0, 3);
                    monthName = monthName.charAt(0) + monthName.substring(1).toLowerCase();

                    periodStats.add(new ChefStatisticsResponse.MonthlyStats(
                            monthName,
                            monthStart.getYear(),
                            monthRevenue != null ? monthRevenue : 0.0,
                            (int) monthOrders,
                            averageRating
                    ));
                }
                break;
                
            case "month":
            default:
                // Show weekly data for last 4 weeks
                for (int i = 3; i >= 0; i--) {
                    LocalDateTime weekStart = now.minusWeeks(i).with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
                    LocalDateTime weekEnd = weekStart.plusWeeks(1).minusSeconds(1);
                    if (weekEnd.isAfter(now)) weekEnd = now;
                    
                    Double weekRevenue = orderRepository.sumTotalPriceByChefIdAndDateRangeAndStatus(
                            chefId, weekStart, weekEnd, OrderStatus.COMPLETED);
                    long weekOrders = orderRepository.countByChefIdAndCreatedAtBetweenAndStatus(
                            chefId, weekStart, weekEnd, OrderStatus.COMPLETED);
                    
                    String label = "Sem " + (4 - i);
                    
                    periodStats.add(new ChefStatisticsResponse.MonthlyStats(
                            label,
                            weekStart.getYear(),
                            weekRevenue != null ? weekRevenue : 0.0,
                            (int) weekOrders,
                            averageRating
                    ));
                }
                break;
        }
        response.setMonthlyStats(periodStats);

        // Recent activities
        List<ChefStatisticsResponse.RecentActivity> activities = new ArrayList<>();

        // Recent orders as activities
        List<Order> recentOrders = orderRepository.findRecentOrdersByChefId(chefId, 3);
        for (Order order : recentOrders) {
            activities.add(new ChefStatisticsResponse.RecentActivity(
                    "order",
                    "Nouvelle commande reçue",
                    formatTimeAgo(order.getCreatedAt()) + " • €" + String.format("%.2f", order.getTotalPrice()),
                    formatTimeAgo(order.getCreatedAt())
            ));
        }

        // Recent reviews as activities
        List<ChefReview> recentReviews = chefReviewRepository.findByChefId(chefId);
        recentReviews.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(2)
                .forEach(review -> {
                    activities.add(new ChefStatisticsResponse.RecentActivity(
                            "review",
                            "Nouvel avis reçu",
                            formatTimeAgo(review.getCreatedAt()) + " • " + review.getRate() + " étoiles",
                            formatTimeAgo(review.getCreatedAt())
                    ));
                });

        response.setRecentActivities(activities);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ChefPlatsStatsResponse getPlatsStats(UUID chefId) {
        logger.info("Getting plats stats for chef: {}", chefId);

        ChefPlatsStatsResponse response = new ChefPlatsStatsResponse();

        // Get all chef's plats
        List<Plat> plats = platRepository.findByChefId(chefId);
        response.setTotalPlats(plats.size());

        LocalDateTime now = LocalDateTime.now();
        double totalRevenue = 0.0;
        int totalOrders = 0;
        int popularCount = 0;

        List<ChefPlatsStatsResponse.PlatWithStats> platsWithStats = new ArrayList<>();

        for (Plat plat : plats) {
            ChefPlatsStatsResponse.PlatWithStats platStats = new ChefPlatsStatsResponse.PlatWithStats();
            platStats.setPlatId(plat.getId());
            platStats.setName(plat.getName());
            platStats.setDescription(plat.getDescription());
            platStats.setImageUrl(plat.getImageUrl());
            platStats.setPrice(plat.getPrice() != null ? plat.getPrice() : 0.0);
            platStats.setCategories(plat.getCategories());
            platStats.setEstimatedCookTime(plat.getEstimatedCookTime());

            // Orders count (all except cancelled and rejected)
            List<OrderStatus> excludedStatuses = Arrays.asList(OrderStatus.CANCELLED, OrderStatus.REJECTED);
            long orders = orderRepository.countByPlatIdAndStatusNotIn(plat.getId(), excludedStatuses);
            platStats.setOrders((int) orders);
            totalOrders += orders;

            // Revenue (completed orders)
            Double revenue = orderRepository.sumTotalPriceByPlatIdAndStatus(plat.getId(), OrderStatus.COMPLETED);
            platStats.setRevenue(revenue != null ? revenue : 0.0);
            totalRevenue += (revenue != null ? revenue : 0.0);

            // Average rating
            double rating = calculatePlatAverageRating(plat.getId());
            platStats.setAverageRating(Math.round(rating * 10.0) / 10.0);

            // Reviews count
            List<PlatReview> reviews = platReviewRepository.findByPlatId(plat.getId());
            platStats.setReviewsCount(reviews.size());

            // Popularity (more than 10 orders is popular)
            boolean isPopular = orders > 10;
            platStats.setPopular(isPopular);
            if (isPopular) popularCount++;

            // Promotion info
            Optional<PromotionPlat> promotion = promotionPlatRepository.findActivePromotionByPlatId(plat.getId(), now);
            platStats.setHasPromotion(promotion.isPresent());
            platStats.setReductionValue(promotion.map(p -> p.getReductionValue().doubleValue()).orElse(null));

            platsWithStats.add(platStats);
        }

        // Sort by orders (most popular first)
        platsWithStats.sort((a, b) -> Integer.compare(b.getOrders(), a.getOrders()));

        response.setPlats(platsWithStats);
        response.setTotalRevenue(totalRevenue);
        response.setTotalOrders(totalOrders);
        response.setPopularPlats(popularCount);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PlatStatisticsResponse getPlatStatistics(UUID platId, UUID chefId) {
        logger.info("Getting statistics for plat: {} by chef: {}", platId, chefId);

        Plat plat = platRepository.findById(platId)
                .orElseThrow(() -> new RuntimeException("Plat not found: " + platId));

        // Verify ownership
        if (!plat.getChefId().equals(chefId)) {
            throw new RuntimeException("Plat does not belong to this chef");
        }

        PlatStatisticsResponse response = new PlatStatisticsResponse();
        response.setPlatId(plat.getId());
        response.setPlatName(plat.getName());
        response.setImageUrl(plat.getImageUrl());
        response.setPrice(plat.getPrice() != null ? plat.getPrice() : 0.0);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startOfWeek = now.minusWeeks(1);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // Total orders (completed)
        long totalOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.COMPLETED);
        response.setTotalOrders((int) totalOrders);

        // Orders this month
        long ordersThisMonth = orderRepository.countByPlatIdAndCreatedAtBetween(platId, startOfMonth, now);
        response.setOrdersThisMonth((int) ordersThisMonth);

        // Orders this week
        long ordersThisWeek = orderRepository.countByPlatIdAndCreatedAtBetween(platId, startOfWeek, now);
        response.setOrdersThisWeek((int) ordersThisWeek);

        // Orders today
        long ordersToday = orderRepository.countByPlatIdAndCreatedAtBetween(platId, startOfToday, endOfToday);
        response.setOrdersToday((int) ordersToday);

        // Total revenue (completed)
        Double totalRevenue = orderRepository.sumTotalPriceByPlatIdAndStatus(platId, OrderStatus.COMPLETED);
        response.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);

        // Revenue this month
        Double revenueThisMonth = orderRepository.sumTotalPriceByPlatIdAndDateRangeAndStatus(
                platId, startOfMonth, now, OrderStatus.COMPLETED);
        response.setRevenueThisMonth(revenueThisMonth != null ? revenueThisMonth : 0.0);

        // Revenue this week
        Double revenueThisWeek = orderRepository.sumTotalPriceByPlatIdAndDateRangeAndStatus(
                platId, startOfWeek, now, OrderStatus.COMPLETED);
        response.setRevenueThisWeek(revenueThisWeek != null ? revenueThisWeek : 0.0);

        // Revenue today
        Double revenueToday = orderRepository.sumTotalPriceByPlatIdAndDateRangeAndStatus(
                platId, startOfToday, endOfToday, OrderStatus.COMPLETED);
        response.setRevenueToday(revenueToday != null ? revenueToday : 0.0);

        // Average rating
        double averageRating = calculatePlatAverageRating(platId);
        response.setAverageRating(Math.round(averageRating * 10.0) / 10.0);

        // Reviews
        List<PlatReview> reviews = platReviewRepository.findByPlatId(platId);
        response.setTotalReviews(reviews.size());

        // Recent reviews
        List<PlatStatisticsResponse.ReviewSummary> recentReviews = reviews.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(review -> {
                    String userName = getUserName(review.getUserId());
                    return new PlatStatisticsResponse.ReviewSummary(
                            review.getId(),
                            userName,
                            review.getRate(),
                            review.getReviewText(),
                            review.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    );
                })
                .collect(Collectors.toList());
        response.setRecentReviews(recentReviews);

        // Popularity
        response.setPopular(totalOrders > 10);

        // Popularity rank among chef's plats
        List<Plat> chefPlats = platRepository.findByChefId(chefId);
        List<UUID> sortedByOrders = chefPlats.stream()
                .sorted((a, b) -> Long.compare(
                        orderRepository.countByPlatIdAndStatus(b.getId(), OrderStatus.COMPLETED),
                        orderRepository.countByPlatIdAndStatus(a.getId(), OrderStatus.COMPLETED)))
                .map(Plat::getId)
                .collect(Collectors.toList());
        response.setPopularityRank(sortedByOrders.indexOf(platId) + 1);

        // Monthly stats for charts (last 7 months)
        List<PlatStatisticsResponse.MonthlyStats> monthlyStats = new ArrayList<>();
        String[] frenchMonths = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            
            long monthOrders = orderRepository.countByPlatIdAndCreatedAtBetween(platId, monthStart, monthEnd);
            Double monthRevenue = orderRepository.sumTotalPriceByPlatIdAndDateRangeAndStatus(
                    platId, monthStart, monthEnd, OrderStatus.COMPLETED);
            
            String monthName = frenchMonths[monthStart.getMonthValue() - 1];
            monthlyStats.add(new PlatStatisticsResponse.MonthlyStats(
                    monthName,
                    (int) monthOrders,
                    monthRevenue != null ? monthRevenue : 0.0
            ));
        }
        response.setMonthlyStats(monthlyStats);

        return response;
    }

    // Helper methods

    private double calculateChefAverageRating(UUID chefId) {
        // First try chef reviews
        List<ChefReview> chefReviews = chefReviewRepository.findByChefId(chefId);
        
        // Also get all plat reviews for this chef's plats
        List<Plat> chefPlats = platRepository.findByChefId(chefId);
        List<PlatReview> platReviews = new ArrayList<>();
        for (Plat plat : chefPlats) {
            platReviews.addAll(platReviewRepository.findByPlatId(plat.getId()));
        }
        
        // Combine all ratings
        double totalSum = 0.0;
        int totalCount = 0;
        
        if (!chefReviews.isEmpty()) {
            totalSum += chefReviews.stream().mapToInt(ChefReview::getRate).sum();
            totalCount += chefReviews.size();
        }
        
        if (!platReviews.isEmpty()) {
            totalSum += platReviews.stream().mapToInt(PlatReview::getRate).sum();
            totalCount += platReviews.size();
        }
        
        if (totalCount == 0) {
            return 0.0;
        }
        
        return totalSum / totalCount;
    }

    private double calculatePlatAverageRating(UUID platId) {
        List<PlatReview> reviews = platReviewRepository.findByPlatId(platId);
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double sum = reviews.stream().mapToInt(PlatReview::getRate).sum();
        return sum / reviews.size();
    }

    private String calculatePercentChange(Double previous, Double current) {
        if (previous == null || previous == 0) {
            if (current == null || current == 0) {
                return "+0%";
            }
            return "+100%";
        }
        double change = ((current - previous) / previous) * 100;
        String sign = change >= 0 ? "+" : "";
        return sign + String.format("%.0f", change) + "%";
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        
        long hours = ChronoUnit.HOURS.between(dateTime, LocalDateTime.now());
        if (hours < 1) {
            long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());
            return minutes + " min";
        } else if (hours < 24) {
            return hours + "h";
        } else {
            long days = ChronoUnit.DAYS.between(dateTime, LocalDateTime.now());
            if (days == 1) return "Hier";
            return days + " jours";
        }
    }

    private String getUserName(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> user.getFirstName() + " " + user.getLastName().charAt(0) + ".")
                .orElse("Utilisateur");
    }

    private ChefDashboardStatsResponse.RecentOrderSummary mapToRecentOrderSummary(Order order) {
        String customerName = getUserName(order.getUserId());
        String platName = platRepository.findById(order.getPlatId())
                .map(Plat::getName)
                .orElse("Plat inconnu");
        String time = order.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        return new ChefDashboardStatsResponse.RecentOrderSummary(
                order.getId(),
                customerName,
                platName,
                time,
                order.getTotalPrice() != null ? order.getTotalPrice() : 0.0,
                order.getStatus().toString().toLowerCase()
        );
    }
}
