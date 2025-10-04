package com.dineo_backend.dineo.orders.service.impl;

import com.dineo_backend.dineo.orders.dto.CreateOrderRequest;
import com.dineo_backend.dineo.orders.dto.OrderResponse;
import com.dineo_backend.dineo.orders.dto.UpdateOrderStatusRequest;
import com.dineo_backend.dineo.orders.enums.OrderStatus;
import com.dineo_backend.dineo.orders.model.Order;
import com.dineo_backend.dineo.orders.repository.OrderRepository;
import com.dineo_backend.dineo.orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService interface
 * Handles all business logic for order management
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        logger.info("Creating new order for user: {} and plat: {}", userId, request.getPlatId());

        Order order = new Order();
        order.setPlatId(request.getPlatId());
        order.setUserId(userId);
        order.setDescription(request.getDescription());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getId());

        return convertToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        logger.info("Retrieving order with ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Commande non trouvée avec l'ID: " + orderId);
                });

        return convertToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        logger.info("Retrieving orders for user: {}", userId);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByPlatId(UUID platId) {
        logger.info("Retrieving orders for plat: {}", platId);

        List<Order> orders = orderRepository.findByPlatIdOrderByCreatedAtDesc(platId);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        logger.info("Retrieving orders with status: {}", status);

        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserIdAndStatus(UUID userId, OrderStatus status) {
        logger.info("Retrieving orders for user: {} with status: {}", userId, status);

        List<Order> orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByPlatIdAndStatus(UUID platId, OrderStatus status) {
        logger.info("Retrieving orders for plat: {} with status: {}", platId, status);

        List<Order> orders = orderRepository.findByPlatIdAndStatusOrderByCreatedAtDesc(platId, status);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, UpdateOrderStatusRequest request) {
        logger.info("Updating order status for order: {} to status: {}", orderId, request.getStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Commande non trouvée avec l'ID: " + orderId);
                });

        order.setStatus(request.getStatus());
        
        if (request.getChefNotes() != null) {
            order.setChefNotes(request.getChefNotes());
        }
        
        if (request.getEstimatedDeliveryTime() != null) {
            order.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        }
        
        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        }

        // Set actual delivery time if order is completed
        if (request.getStatus() == OrderStatus.COMPLETED) {
            order.setActualDeliveryTime(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully for order: {}", orderId);

        return convertToOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse updateOrderDeliveryAddress(UUID orderId, String deliveryAddress, UUID userId) {
        logger.info("Updating delivery address for order: {} by user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Commande non trouvée avec l'ID: " + orderId);
                });

        // Check if the user is the owner of the order
        if (!order.getUserId().equals(userId)) {
            logger.error("User {} is not authorized to update order {}", userId, orderId);
            throw new RuntimeException("Vous n'êtes pas autorisé à modifier cette commande");
        }

        // Check if order can still be modified
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            logger.error("Cannot update delivery address for order {} with status {}", orderId, order.getStatus());
            throw new RuntimeException("Impossible de modifier l'adresse de livraison pour cette commande");
        }

        order.setDeliveryAddress(deliveryAddress);
        Order updatedOrder = orderRepository.save(order);
        
        logger.info("Delivery address updated successfully for order: {}", orderId);
        return convertToOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId, UUID userId) {
        logger.info("Cancelling order: {} by user: {}", orderId, userId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Commande non trouvée avec l'ID: " + orderId);
                });

        // Check if the user is the owner of the order
        if (!order.getUserId().equals(userId)) {
            logger.error("User {} is not authorized to cancel order {}", userId, orderId);
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette commande");
        }

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            logger.error("Cannot cancel order {} with status {}", orderId, order.getStatus());
            throw new RuntimeException("Impossible d'annuler cette commande");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        
        logger.info("Order cancelled successfully: {}", orderId);
        return convertToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving orders between {} and {}", startDate, endDate);

        List<Order> orders = orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserIdAndDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Retrieving orders for user: {} between {} and {}", userId, startDate, endDate);

        List<Order> orders = orderRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatisticsByUserId(UUID userId) {
        logger.info("Retrieving order statistics for user: {}", userId);

        long totalOrders = orderRepository.countByUserId(userId);
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CONFIRMED);
        long preparingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PREPARING);
        long readyOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.READY);
        long completedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED);
        long rejectedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.REJECTED);

        return new OrderStatistics(totalOrders, pendingOrders, confirmedOrders, preparingOrders,
                readyOrders, completedOrders, cancelledOrders, rejectedOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatistics getOrderStatisticsByPlatId(UUID platId) {
        logger.info("Retrieving order statistics for plat: {}", platId);

        long totalOrders = orderRepository.countByPlatId(platId);
        long pendingOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.PENDING);
        long confirmedOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.CONFIRMED);
        long preparingOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.PREPARING);
        long readyOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.READY);
        long completedOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.CANCELLED);
        long rejectedOrders = orderRepository.countByPlatIdAndStatus(platId, OrderStatus.REJECTED);

        return new OrderStatistics(totalOrders, pendingOrders, confirmedOrders, preparingOrders,
                readyOrders, completedOrders, cancelledOrders, rejectedOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPendingOrdersForPlat(UUID userId, UUID platId) {
        logger.info("Checking if user: {} has pending orders for plat: {}", userId, platId);

        return orderRepository.existsByUserIdAndPlatIdAndStatus(userId, platId, OrderStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMostRecentOrderByUserId(UUID userId) {
        logger.info("Retrieving most recent order for user: {}", userId);

        return orderRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(this::convertToOrderResponse)
                .orElse(null);
    }

    @Override
    public OrderResponse convertToOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return new OrderResponse(
                order.getId(),
                order.getPlatId(),
                order.getUserId(),
                order.getDescription(),
                order.getStatus(),
                order.getChefNotes(),
                order.getDeliveryAddress(),
                order.getEstimatedDeliveryTime(),
                order.getActualDeliveryTime(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}