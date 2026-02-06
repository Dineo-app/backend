package com.dineo_backend.dineo.administration.dto;

/**
 * DTO for admin dashboard statistics
 */
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalChefs;
    private Long totalDishes;
    private Long totalOrders;
    private Long todayOrders;

    public AdminStatsResponse() {
    }

    public AdminStatsResponse(Long totalUsers, Long totalChefs, Long totalDishes, Long totalOrders, Long todayOrders) {
        this.totalUsers = totalUsers;
        this.totalChefs = totalChefs;
        this.totalDishes = totalDishes;
        this.totalOrders = totalOrders;
        this.todayOrders = todayOrders;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalChefs() {
        return totalChefs;
    }

    public void setTotalChefs(Long totalChefs) {
        this.totalChefs = totalChefs;
    }

    public Long getTotalDishes() {
        return totalDishes;
    }

    public void setTotalDishes(Long totalDishes) {
        this.totalDishes = totalDishes;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Long getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(Long todayOrders) {
        this.todayOrders = todayOrders;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long totalUsers;
        private Long totalChefs;
        private Long totalDishes;
        private Long totalOrders;
        private Long todayOrders;

        public Builder totalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
            return this;
        }

        public Builder totalChefs(Long totalChefs) {
            this.totalChefs = totalChefs;
            return this;
        }

        public Builder totalDishes(Long totalDishes) {
            this.totalDishes = totalDishes;
            return this;
        }

        public Builder totalOrders(Long totalOrders) {
            this.totalOrders = totalOrders;
            return this;
        }

        public Builder todayOrders(Long todayOrders) {
            this.todayOrders = todayOrders;
            return this;
        }

        public AdminStatsResponse build() {
            return new AdminStatsResponse(totalUsers, totalChefs, totalDishes, totalOrders, todayOrders);
        }
    }
}
