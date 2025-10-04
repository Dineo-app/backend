package com.dineo_backend.dineo.orders.service;

import com.dineo_backend.dineo.orders.dto.CreateOrderRequest;
import com.dineo_backend.dineo.orders.dto.OrderResponse;
import com.dineo_backend.dineo.orders.dto.UpdateOrderStatusRequest;
import com.dineo_backend.dineo.orders.enums.OrderStatus;
import com.dineo_backend.dineo.orders.model.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for order management operations
 * Defines business logic methods for order handling
 */
public interface OrderService {

    /**
     * Create a new order
     * @param request the order creation request
     * @param userId the ID of the user creating the order
     * @return the created order response
     */
    OrderResponse createOrder(CreateOrderRequest request, UUID userId);

    /**
     * Get order by ID
     * @param orderId the ID of the order
     * @return the order response
     */
    OrderResponse getOrderById(UUID orderId);

    /**
     * Get all orders for a specific user
     * @param userId the ID of the user
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByUserId(UUID userId);

    /**
     * Get all orders for a specific plat
     * @param platId the ID of the plat
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByPlatId(UUID platId);

    /**
     * Get orders by status
     * @param status the order status
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    /**
     * Get orders for a user filtered by status
     * @param userId the ID of the user
     * @param status the order status
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByUserIdAndStatus(UUID userId, OrderStatus status);

    /**
     * Get orders for a plat filtered by status
     * @param platId the ID of the plat
     * @param status the order status
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByPlatIdAndStatus(UUID platId, OrderStatus status);

    /**
     * Update order status
     * @param orderId the ID of the order
     * @param request the status update request
     * @return the updated order response
     */
    OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request);

    /**
     * Update order delivery address
     * @param orderId the ID of the order
     * @param deliveryAddress the new delivery address
     * @param userId the ID of the user (for authorization)
     * @return the updated order response
     */
    OrderResponse updateOrderDeliveryAddress(UUID orderId, String deliveryAddress, UUID userId);

    /**
     * Cancel order (only by the user who created it)
     * @param orderId the ID of the order
     * @param userId the ID of the user
     * @return the updated order response
     */
    OrderResponse cancelOrder(UUID orderId, UUID userId);

    /**
     * Get orders within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get orders for a user within a date range
     * @param userId the ID of the user
     * @param startDate the start date
     * @param endDate the end date
     * @return list of order responses
     */
    List<OrderResponse> getOrdersByUserIdAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get order statistics for a user
     * @param userId the ID of the user
     * @return order statistics
     */
    OrderStatistics getOrderStatisticsByUserId(UUID userId);

    /**
     * Get order statistics for a plat
     * @param platId the ID of the plat
     * @return order statistics
     */
    OrderStatistics getOrderStatisticsByPlatId(UUID platId);

    /**
     * Check if user has pending orders for a specific plat
     * @param userId the ID of the user
     * @param platId the ID of the plat
     * @return true if user has pending orders
     */
    boolean hasPendingOrdersForPlat(UUID userId, UUID platId);

    /**
     * Get the most recent order for a user
     * @param userId the ID of the user
     * @return the most recent order response or null
     */
    OrderResponse getMostRecentOrderByUserId(UUID userId);

    /**
     * Convert Order entity to OrderResponse DTO
     * @param order the order entity
     * @return the order response DTO
     */
    OrderResponse convertToOrderResponse(Order order);

    /**
     * Inner class for order statistics
     */
    class OrderStatistics {
        private long totalOrders;
        private long pendingOrders;
        private long confirmedOrders;
        private long preparingOrders;
        private long readyOrders;
        private long completedOrders;
        private long cancelledOrders;
        private long rejectedOrders;

        public OrderStatistics() {}

        public OrderStatistics(long totalOrders, long pendingOrders, long confirmedOrders,
                              long preparingOrders, long readyOrders, long completedOrders,
                              long cancelledOrders, long rejectedOrders) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.confirmedOrders = confirmedOrders;
            this.preparingOrders = preparingOrders;
            this.readyOrders = readyOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.rejectedOrders = rejectedOrders;
        }

        // Getters and Setters
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

        public long getPendingOrders() { return pendingOrders; }
        public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

        public long getConfirmedOrders() { return confirmedOrders; }
        public void setConfirmedOrders(long confirmedOrders) { this.confirmedOrders = confirmedOrders; }

        public long getPreparingOrders() { return preparingOrders; }
        public void setPreparingOrders(long preparingOrders) { this.preparingOrders = preparingOrders; }

        public long getReadyOrders() { return readyOrders; }
        public void setReadyOrders(long readyOrders) { this.readyOrders = readyOrders; }

        public long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }

        public long getCancelledOrders() { return cancelledOrders; }
        public void setCancelledOrders(long cancelledOrders) { this.cancelledOrders = cancelledOrders; }

        public long getRejectedOrders() { return rejectedOrders; }
        public void setRejectedOrders(long rejectedOrders) { this.rejectedOrders = rejectedOrders; }
    }
}