package com.dineo_backend.dineo.chefs.dto;

import java.util.List;

/**
 * DTO for chef comprehensive statistics
 * Contains detailed stats for the statistics page with time-based breakdown
 */
public class ChefStatisticsResponse {

    // Overall stats
    private double totalRevenue;
    private int totalOrders;
    private double averageRating;
    private int profileViews; // TODO: implement view tracking
    
    // Change percentages compared to last period
    private String revenueChange; // e.g., "+18%"
    private String ordersChange; // e.g., "+23%"
    private String ratingChange; // e.g., "+0.2"
    private String viewsChange; // e.g., "+12%"
    
    // Monthly breakdown for charts
    private List<MonthlyStats> monthlyStats;
    
    // Recent activity
    private List<RecentActivity> recentActivities;

    // Default constructor
    public ChefStatisticsResponse() {}

    // Getters and Setters
    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getProfileViews() {
        return profileViews;
    }

    public void setProfileViews(int profileViews) {
        this.profileViews = profileViews;
    }

    public String getRevenueChange() {
        return revenueChange;
    }

    public void setRevenueChange(String revenueChange) {
        this.revenueChange = revenueChange;
    }

    public String getOrdersChange() {
        return ordersChange;
    }

    public void setOrdersChange(String ordersChange) {
        this.ordersChange = ordersChange;
    }

    public String getRatingChange() {
        return ratingChange;
    }

    public void setRatingChange(String ratingChange) {
        this.ratingChange = ratingChange;
    }

    public String getViewsChange() {
        return viewsChange;
    }

    public void setViewsChange(String viewsChange) {
        this.viewsChange = viewsChange;
    }

    public List<MonthlyStats> getMonthlyStats() {
        return monthlyStats;
    }

    public void setMonthlyStats(List<MonthlyStats> monthlyStats) {
        this.monthlyStats = monthlyStats;
    }

    public List<RecentActivity> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<RecentActivity> recentActivities) {
        this.recentActivities = recentActivities;
    }

    /**
     * Inner class for monthly statistics
     */
    public static class MonthlyStats {
        private String month; // e.g., "Jan", "Feb"
        private int year;
        private double revenue;
        private int orders;
        private double rating;

        public MonthlyStats() {}

        public MonthlyStats(String month, int year, double revenue, int orders, double rating) {
            this.month = month;
            this.year = year;
            this.revenue = revenue;
            this.orders = orders;
            this.rating = rating;
        }

        // Getters and Setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }

        public int getOrders() { return orders; }
        public void setOrders(int orders) { this.orders = orders; }

        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }
    }

    /**
     * Inner class for recent activity
     */
    public static class RecentActivity {
        private String type; // "order", "review", "view"
        private String title;
        private String description;
        private String timeAgo; // e.g., "2 hours ago"

        public RecentActivity() {}

        public RecentActivity(String type, String title, String description, String timeAgo) {
            this.type = type;
            this.title = title;
            this.description = description;
            this.timeAgo = timeAgo;
        }

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getTimeAgo() { return timeAgo; }
        public void setTimeAgo(String timeAgo) { this.timeAgo = timeAgo; }
    }
}
