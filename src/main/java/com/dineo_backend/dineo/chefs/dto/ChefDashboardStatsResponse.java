package com.dineo_backend.dineo.chefs.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for chef dashboard statistics
 * Contains quick stats for the chef's dashboard home page
 */
public class ChefDashboardStatsResponse {

    // Today's stats
    private int ordersToday;
    private double revenueToday;
    
    // This month stats
    private int ordersThisMonth;
    private double revenueThisMonth;
    private String revenueChangePercent; // e.g., "+12%" or "-5%"
    
    // Overall stats
    private int totalPlats;
    private int newPlatsThisMonth;
    private double averageRating;
    private int totalReviews;
    private String ratingPercent; // satisfaction percentage e.g., "95%"
    
    // Recent orders
    private List<RecentOrderSummary> recentOrders;

    // Default constructor
    public ChefDashboardStatsResponse() {}

    // Getters and Setters
    public int getOrdersToday() {
        return ordersToday;
    }

    public void setOrdersToday(int ordersToday) {
        this.ordersToday = ordersToday;
    }

    public double getRevenueToday() {
        return revenueToday;
    }

    public void setRevenueToday(double revenueToday) {
        this.revenueToday = revenueToday;
    }

    public int getOrdersThisMonth() {
        return ordersThisMonth;
    }

    public void setOrdersThisMonth(int ordersThisMonth) {
        this.ordersThisMonth = ordersThisMonth;
    }

    public double getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(double revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }

    public String getRevenueChangePercent() {
        return revenueChangePercent;
    }

    public void setRevenueChangePercent(String revenueChangePercent) {
        this.revenueChangePercent = revenueChangePercent;
    }

    public int getTotalPlats() {
        return totalPlats;
    }

    public void setTotalPlats(int totalPlats) {
        this.totalPlats = totalPlats;
    }

    public int getNewPlatsThisMonth() {
        return newPlatsThisMonth;
    }

    public void setNewPlatsThisMonth(int newPlatsThisMonth) {
        this.newPlatsThisMonth = newPlatsThisMonth;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public String getRatingPercent() {
        return ratingPercent;
    }

    public void setRatingPercent(String ratingPercent) {
        this.ratingPercent = ratingPercent;
    }

    public List<RecentOrderSummary> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<RecentOrderSummary> recentOrders) {
        this.recentOrders = recentOrders;
    }

    /**
     * Inner class for recent order summary
     */
    public static class RecentOrderSummary {
        private UUID orderId;
        private String customerName;
        private String platName;
        private String time;
        private double amount;
        private String status;

        public RecentOrderSummary() {}

        public RecentOrderSummary(UUID orderId, String customerName, String platName, 
                                   String time, double amount, String status) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.platName = platName;
            this.time = time;
            this.amount = amount;
            this.status = status;
        }

        // Getters and Setters
        public UUID getOrderId() { return orderId; }
        public void setOrderId(UUID orderId) { this.orderId = orderId; }

        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }

        public String getPlatName() { return platName; }
        public void setPlatName(String platName) { this.platName = platName; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
