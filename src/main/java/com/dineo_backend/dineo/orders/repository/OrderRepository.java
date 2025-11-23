 package com.dineo_backend.dineo.orders.repository;

import com.dineo_backend.dineo.orders.enums.OrderStatus;
import com.dineo_backend.dineo.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Order entity operations
 * Provides comprehensive query methods for order management
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Find all orders for a specific user
     * @param userId the ID of the user
     * @return list of orders for the user
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all orders for a specific plat
     * @param platId the ID of the plat
     * @return list of orders for the plat
     */
    List<Order> findByPlatIdOrderByCreatedAtDesc(UUID platId);

    /**
     * Find orders by user and status
     * @param userId the ID of the user
     * @param status the order status
     * @return list of orders matching criteria
     */
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderStatus status);

    /**
     * Find orders by plat and status
     * @param platId the ID of the plat
     * @param status the order status
     * @return list of orders matching criteria
     */
    List<Order> findByPlatIdAndStatusOrderByCreatedAtDesc(UUID platId, OrderStatus status);

    /**
     * Find all orders with specific status
     * @param status the order status
     * @return list of orders with the status
     */
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    /**
     * Find orders created within a date range
     * @param startDate the start date
     * @param endDate the end date
     * @return list of orders created within the range
     */
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders for a specific user within a date range
     * @param userId the ID of the user
     * @param startDate the start date
     * @param endDate the end date
     * @return list of orders matching criteria
     */
    List<Order> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find orders for a specific plat within a date range
     * @param platId the ID of the plat
     * @param startDate the start date
     * @param endDate the end date
     * @return list of orders matching criteria
     */
    List<Order> findByPlatIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID platId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count total orders for a user
     * @param userId the ID of the user
     * @return number of orders
     */
    long countByUserId(UUID userId);

    /**
     * Count orders by status for a user
     * @param userId the ID of the user
     * @param status the order status
     * @return number of orders with the status
     */
    long countByUserIdAndStatus(UUID userId, OrderStatus status);

    /**
     * Count total orders for a plat
     * @param platId the ID of the plat
     * @return number of orders
     */
    long countByPlatId(UUID platId);

    /**
     * Count orders by status for a plat
     * @param platId the ID of the plat
     * @param status the order status
     * @return number of orders with the status
     */
    long countByPlatIdAndStatus(UUID platId, OrderStatus status);

    /**
     * Check if user has any pending orders for a specific plat
     * @param userId the ID of the user
     * @param platId the ID of the plat
     * @return true if user has pending orders for the plat
     */
    boolean existsByUserIdAndPlatIdAndStatus(UUID userId, UUID platId, OrderStatus status);

    /**
     * Find the most recent order for a user
     * @param userId the ID of the user
     * @return optional containing the most recent order
     */
    Optional<Order> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find the most recent order for a plat
     * @param platId the ID of the plat
     * @return optional containing the most recent order
     */
    Optional<Order> findTopByPlatIdOrderByCreatedAtDesc(UUID platId);

    /**
     * Find orders that need status update (e.g., estimated delivery time passed)
     * @param currentTime the current time to compare against
     * @param statuses list of statuses to check
     * @return list of orders that may need status update
     */
    @Query("SELECT o FROM Order o WHERE o.estimatedDeliveryTime <= :currentTime AND o.status IN :statuses")
    List<Order> findOrdersNeedingStatusUpdate(@Param("currentTime") LocalDateTime currentTime, 
                                             @Param("statuses") List<OrderStatus> statuses);

    /**
     * Find orders by multiple statuses
     * @param statuses list of order statuses
     * @return list of orders with any of the specified statuses
     */
    List<Order> findByStatusInOrderByCreatedAtDesc(List<OrderStatus> statuses);

    /**
     * Find orders for a user by multiple statuses
     * @param userId the ID of the user
     * @param statuses list of order statuses
     * @return list of orders matching criteria
     */
    List<Order> findByUserIdAndStatusInOrderByCreatedAtDesc(UUID userId, List<OrderStatus> statuses);

    /**
     * Find orders for a plat by multiple statuses
     * @param platId the ID of the plat
     * @param statuses list of order statuses
     * @return list of orders matching criteria
     */
    List<Order> findByPlatIdAndStatusInOrderByCreatedAtDesc(UUID platId, List<OrderStatus> statuses);

    /**
     * Find all orders for a specific chef
     * @param chefId the ID of the chef
     * @return list of orders for the chef
     */
    List<Order> findByChefIdOrderByCreatedAtDesc(UUID chefId);

    /**
     * Find orders for a specific chef filtered by status
     * @param chefId the ID of the chef
     * @param status the order status
     * @return list of orders matching criteria
     */
    List<Order> findByChefIdAndStatusOrderByCreatedAtDesc(UUID chefId, OrderStatus status);

    /**
     * Count total orders for a chef
     * @param chefId the ID of the chef
     * @return number of orders
     */
    long countByChefId(UUID chefId);

    /**
     * Count orders by status for a chef
     * @param chefId the ID of the chef
     * @param status the order status
     * @return number of orders with the status
     */
    long countByChefIdAndStatus(UUID chefId, OrderStatus status);
}