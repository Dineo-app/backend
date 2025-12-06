package com.dineo_backend.dineo.plats.dto;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for individual plat statistics
 * Contains detailed stats for a specific plat
 */
public class PlatStatisticsResponse {

    private UUID platId;
    private String platName;
    private String imageUrl;
    private double price;
    
    // Order stats
    private int totalOrders;
    private int ordersThisMonth;
    private int ordersThisWeek;
    private int ordersToday;
    
    // Revenue stats
    private double totalRevenue;
    private double revenueThisMonth;
    private double revenueThisWeek;
    private double revenueToday;
    
    // Review stats
    private double averageRating;
    private int totalReviews;
    private List<ReviewSummary> recentReviews;
    
    // Popularity
    private boolean isPopular;
    private int popularityRank; // Rank among chef's plats
    
    // Monthly statistics for charts
    private List<MonthlyStats> monthlyStats;

    // Default constructor
    public PlatStatisticsResponse() {}

    // Getters and Setters
    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
    }

    public String getPlatName() {
        return platName;
    }

    public void setPlatName(String platName) {
        this.platName = platName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public int getOrdersThisMonth() {
        return ordersThisMonth;
    }

    public void setOrdersThisMonth(int ordersThisMonth) {
        this.ordersThisMonth = ordersThisMonth;
    }

    public int getOrdersThisWeek() {
        return ordersThisWeek;
    }

    public void setOrdersThisWeek(int ordersThisWeek) {
        this.ordersThisWeek = ordersThisWeek;
    }

    public int getOrdersToday() {
        return ordersToday;
    }

    public void setOrdersToday(int ordersToday) {
        this.ordersToday = ordersToday;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(double revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }

    public double getRevenueThisWeek() {
        return revenueThisWeek;
    }

    public void setRevenueThisWeek(double revenueThisWeek) {
        this.revenueThisWeek = revenueThisWeek;
    }

    public double getRevenueToday() {
        return revenueToday;
    }

    public void setRevenueToday(double revenueToday) {
        this.revenueToday = revenueToday;
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

    public List<ReviewSummary> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(List<ReviewSummary> recentReviews) {
        this.recentReviews = recentReviews;
    }

    @JsonProperty("isPopular")
    public boolean isPopular() {
        return isPopular;
    }

    public void setPopular(boolean popular) {
        isPopular = popular;
    }

    public int getPopularityRank() {
        return popularityRank;
    }

    public void setPopularityRank(int popularityRank) {
        this.popularityRank = popularityRank;
    }

    public List<MonthlyStats> getMonthlyStats() {
        return monthlyStats;
    }

    public void setMonthlyStats(List<MonthlyStats> monthlyStats) {
        this.monthlyStats = monthlyStats;
    }

    /**
     * Inner class for monthly statistics
     */
    public static class MonthlyStats {
        private String month;
        private int orders;
        private double revenue;

        public MonthlyStats() {}

        public MonthlyStats(String month, int orders, double revenue) {
            this.month = month;
            this.orders = orders;
            this.revenue = revenue;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public int getOrders() { return orders; }
        public void setOrders(int orders) { this.orders = orders; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }
    }

    /**
     * Inner class for review summary
     */
    public static class ReviewSummary {
        private UUID reviewId;
        private String userName;
        private int rating;
        private String comment;
        private String date;

        public ReviewSummary() {}

        public ReviewSummary(UUID reviewId, String userName, int rating, String comment, String date) {
            this.reviewId = reviewId;
            this.userName = userName;
            this.rating = rating;
            this.comment = comment;
            this.date = date;
        }

        // Getters and Setters
        public UUID getReviewId() { return reviewId; }
        public void setReviewId(UUID reviewId) { this.reviewId = reviewId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
    }
}
