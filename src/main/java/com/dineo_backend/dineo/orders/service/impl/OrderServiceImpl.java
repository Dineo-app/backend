package com.dineo_backend.dineo.orders.service.impl;

import com.dineo_backend.dineo.orders.dto.CreateOrderRequest;
import com.dineo_backend.dineo.orders.dto.OrderResponse;
import com.dineo_backend.dineo.orders.dto.UpdateOrderStatusRequest;
import com.dineo_backend.dineo.orders.enums.OrderStatus;
import com.dineo_backend.dineo.orders.model.Order;
import com.dineo_backend.dineo.orders.model.OrderIngredient;
import com.dineo_backend.dineo.orders.repository.OrderRepository;
import com.dineo_backend.dineo.orders.repository.OrderIngredientRepository;
import com.dineo_backend.dineo.orders.service.OrderService;
import com.dineo_backend.dineo.websocket.OrderNotificationService;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.repository.IngredientRepository;
import com.dineo_backend.dineo.plats.model.Plat;
import com.dineo_backend.dineo.plats.model.Ingredient;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
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

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderIngredientRepository orderIngredientRepository;

    @Autowired
    private OrderNotificationService notificationService;

    @Autowired
    private PlatRepository platRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        logger.info("Creating new order for user: {} and plat: {} with quantity: {}", userId, request.getPlatId(), request.getQuantity());

        // SECURITY: Verify payment before creating order
        if (request.getPaymentIntentId() == null || request.getPaymentIntentId().trim().isEmpty()) {
            logger.warn("Payment intent ID is missing for order creation - OLD APP VERSION DETECTED");
            throw new RuntimeException("⚠️ Veuillez mettre à jour l'application depuis TestFlight pour passer commande avec le nouveau système de paiement sécurisé.");
        }

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
            
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                logger.error("Payment not succeeded. Status: {}", paymentIntent.getStatus());
                throw new RuntimeException("Le paiement n'a pas été confirmé. Statut: " + paymentIntent.getStatus());
            }
            
            logger.info("Payment verified successfully: {}", request.getPaymentIntentId());
        } catch (StripeException e) {
            logger.error("Failed to verify payment: {}", e.getMessage());
            throw new RuntimeException("Impossible de vérifier le paiement: " + e.getMessage());
        }

        // Fetch plat to get price
        Plat plat = platRepository.findById(request.getPlatId())
                .orElseThrow(() -> {
                    logger.error("Plat not found with ID: {}", request.getPlatId());
                    return new RuntimeException("Plat non trouvé avec l'ID: " + request.getPlatId());
                });

        // Calculate total price (base price + paid ingredients)
        Double totalPrice = plat.getPrice() * request.getQuantity();
        
        // Add paid ingredients price
        if (request.getSelectedIngredientIds() != null && !request.getSelectedIngredientIds().isEmpty()) {
            List<Ingredient> selectedIngredients = ingredientRepository.findAllById(request.getSelectedIngredientIds());
            Double ingredientsPrice = selectedIngredients.stream()
                    .filter(ing -> !ing.getIsFree())
                    .mapToDouble(Ingredient::getPrice)
                    .sum();
            totalPrice += ingredientsPrice * request.getQuantity();
            logger.info("Added ingredients price: {} for {} items", ingredientsPrice * request.getQuantity(), request.getQuantity());
        }

        Order order = new Order();
        order.setPlatId(request.getPlatId());
        order.setUserId(userId);
        order.setChefId(request.getChefId());
        order.setDescription(request.getDescription());
        order.setQuantity(request.getQuantity());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(totalPrice);

        Order savedOrder = orderRepository.save(order);
        
        // Save selected ingredients
        if (request.getSelectedIngredientIds() != null && !request.getSelectedIngredientIds().isEmpty()) {
            List<Ingredient> selectedIngredients = ingredientRepository.findAllById(request.getSelectedIngredientIds());
            for (Ingredient ingredient : selectedIngredients) {
                OrderIngredient orderIngredient = new OrderIngredient(
                        savedOrder.getId(),
                        ingredient.getId(),
                        ingredient.getName(),
                        ingredient.getPrice(),
                        ingredient.getIsFree(),
                        1
                );
                orderIngredientRepository.save(orderIngredient);
            }
            logger.info("Saved {} ingredients for order {}", selectedIngredients.size(), savedOrder.getId());
        }
        
        logger.info("Order created successfully with ID: {} and total price: {}", savedOrder.getId(), totalPrice);

        OrderResponse orderResponse = convertToOrderResponse(savedOrder);
        
        // Send real-time notification to chef
        notificationService.notifyChefNewOrder(request.getChefId(), orderResponse);

        return orderResponse;
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
        
        if (request.getOrderNotes() != null) {
            order.setOrderNotes(request.getOrderNotes());
        }
        
        if (request.getEstimatedDeliveryTime() != null) {
            order.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        }
        
        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        }

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully for order: {}", orderId);

        OrderResponse orderResponse = convertToOrderResponse(updatedOrder);
        
        // Send real-time notification to user about status change
        notificationService.notifyUserOrderProgress(order.getUserId(), orderResponse);

        return orderResponse;
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
        
        OrderResponse orderResponse = convertToOrderResponse(updatedOrder);
        
        // Send real-time notification to chef about cancellation
        notificationService.notifyChefOrderCancelled(order.getChefId(), orderResponse);
        
        return orderResponse;
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
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByChefId(UUID chefId) {
        logger.info("Retrieving orders for chef: {}", chefId);

        List<Order> orders = orderRepository.findByChefIdOrderByCreatedAtDesc(chefId);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse acceptOrder(UUID orderId, UUID chefId) {
        logger.info("Chef {} accepting order: {}", chefId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Commande non trouvée avec l'ID: " + orderId);
                });

        // Verify that the order belongs to this chef
        if (!order.getChefId().equals(chefId)) {
            logger.error("Chef {} is not authorized to accept order {}", chefId, orderId);
            throw new RuntimeException("Vous n'êtes pas autorisé à accepter cette commande");
        }

        // Only PENDING orders can be accepted
        if (order.getStatus() != OrderStatus.PENDING) {
            logger.error("Cannot accept order {} with status {}", orderId, order.getStatus());
            throw new RuntimeException("Seules les commandes en attente peuvent être acceptées");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order {} accepted successfully by chef {}", orderId, chefId);

        OrderResponse orderResponse = convertToOrderResponse(updatedOrder);
        
        // Send real-time notification to user
        notificationService.notifyUserOrderAccepted(order.getUserId(), orderResponse);

        return orderResponse;
    }

    @Override
    public OrderResponse rejectOrder(UUID orderId, UUID chefId) {
        logger.info("Chef {} rejecting order: {}", chefId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", orderId);
                    return new RuntimeException("Commande non trouvée avec l'ID: " + orderId);
                });

        // Verify that the order belongs to this chef
        if (!order.getChefId().equals(chefId)) {
            logger.error("Chef {} is not authorized to reject order {}", chefId, orderId);
            throw new RuntimeException("Vous n'êtes pas autorisé à rejeter cette commande");
        }

        // Only PENDING orders can be rejected
        if (order.getStatus() != OrderStatus.PENDING) {
            logger.error("Cannot reject order {} with status {}", orderId, order.getStatus());
            throw new RuntimeException("Seules les commandes en attente peuvent être rejetées");
        }

        order.setStatus(OrderStatus.REJECTED);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order {} rejected successfully by chef {}", orderId, chefId);

        OrderResponse orderResponse = convertToOrderResponse(updatedOrder);
        
        // Send real-time notification to user
        notificationService.notifyUserOrderRejected(order.getUserId(), orderResponse);

        return orderResponse;
    }

    @Override
    public OrderResponse convertToOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        // Fetch customer information
        User customer = userRepository.findById(order.getUserId()).orElse(null);

        // Fetch order ingredients
        List<OrderIngredient> orderIngredients = orderIngredientRepository.findByOrderId(order.getId());
        List<OrderResponse.OrderIngredientDTO> ingredientDTOs = orderIngredients.stream()
                .map(oi -> new OrderResponse.OrderIngredientDTO(
                        oi.getIngredientId(),
                        oi.getIngredientName(),
                        oi.getQuantity(),
                        oi.getIsFree()
                ))
                .collect(Collectors.toList());

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getPlatId(),
                order.getUserId(),
                order.getChefId(),
                order.getDescription(),
                order.getStatus(),
                order.getQuantity(),
                order.getOrderNotes(),
                order.getDeliveryAddress(),
                order.getEstimatedDeliveryTime(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
        
        // Set customer information
        if (customer != null) {
            response.setCustomerFirstName(customer.getFirstName());
            response.setCustomerLastName(customer.getLastName());
            response.setCustomerPhone(customer.getPhone());
            response.setCustomerEmail(customer.getEmail());
        }
        
        response.setSelectedIngredients(ingredientDTOs);
        return response;
    }
}