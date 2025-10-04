package com.dineo_backend.dineo.orders.controller;

import com.dineo_backend.dineo.authentication.service.JwtService;
import com.dineo_backend.dineo.config.AppConstants;
import com.dineo_backend.dineo.orders.dto.CreateOrderRequest;
import com.dineo_backend.dineo.orders.dto.OrderResponse;
import com.dineo_backend.dineo.orders.dto.UpdateOrderStatusRequest;
import com.dineo_backend.dineo.orders.enums.OrderStatus;
import com.dineo_backend.dineo.orders.service.OrderService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for order management operations
 * Handles HTTP requests for order-related endpoints
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtService jwtService;

    /**
     * Create a new order
     * @param request the order creation request
     * @param authHeader the authorization header containing JWT token
     * @return the created order response
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Creating new order for plat: {}", request.getPlatId());

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            OrderResponse orderResponse = orderService.createOrder(request, userId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(AppConstants.ORDER_CREATED_SUCCESS, orderResponse));

        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), AppConstants.ORDER_CREATION_FAILED));
        }
    }

    /**
     * Get order by ID
     * @param orderId the ID of the order
     * @return the order response
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable UUID orderId) {
        try {
            logger.info("Retrieving order with ID: {}", orderId);

            OrderResponse orderResponse = orderService.getOrderById(orderId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDER_RETRIEVED_SUCCESS, orderResponse));

        } catch (RuntimeException e) {
            logger.error("Error retrieving order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error retrieving order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get all orders for the authenticated user
     * @param authHeader the authorization header containing JWT token
     * @return list of user's orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Retrieving orders for authenticated user");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDERS_RETRIEVED_SUCCESS, orders));

        } catch (Exception e) {
            logger.error("Error retrieving user orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get orders for a specific plat
     * @param platId the ID of the plat
     * @return list of orders for the plat
     */
    @GetMapping("/plat/{platId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByPlatId(@PathVariable UUID platId) {
        try {
            logger.info("Retrieving orders for plat: {}", platId);

            List<OrderResponse> orders = orderService.getOrdersByPlatId(platId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDERS_RETRIEVED_SUCCESS, orders));

        } catch (Exception e) {
            logger.error("Error retrieving orders for plat {}: {}", platId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get orders by status
     * @param status the order status
     * @return list of orders with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            logger.info("Retrieving orders with status: {}", status);

            List<OrderResponse> orders = orderService.getOrdersByStatus(status);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDERS_RETRIEVED_SUCCESS, orders));

        } catch (Exception e) {
            logger.error("Error retrieving orders with status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get orders for authenticated user filtered by status
     * @param status the order status
     * @param authHeader the authorization header containing JWT token
     * @return list of user's orders with the specified status
     */
    @GetMapping("/my-orders/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrdersByStatus(
            @PathVariable OrderStatus status,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Retrieving orders with status {} for authenticated user", status);

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            List<OrderResponse> orders = orderService.getOrdersByUserIdAndStatus(userId, status);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDERS_RETRIEVED_SUCCESS, orders));

        } catch (Exception e) {
            logger.error("Error retrieving user orders with status {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Update order status (for chefs and administrators)
     * @param orderId the ID of the order
     * @param request the status update request
     * @return the updated order response
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        
        try {
            logger.info("Updating status for order: {} to status: {}", orderId, request.getStatus());

            OrderResponse orderResponse = orderService.updateOrderStatus(orderId, request);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDER_STATUS_UPDATED_SUCCESS, orderResponse));

        } catch (RuntimeException e) {
            logger.error("Error updating order status for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating order status for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Update delivery address for an order
     * @param orderId the ID of the order
     * @param deliveryAddress the new delivery address
     * @param authHeader the authorization header containing JWT token
     * @return the updated order response
     */
    @PutMapping("/{orderId}/delivery-address")
    public ResponseEntity<ApiResponse<OrderResponse>> updateDeliveryAddress(
            @PathVariable UUID orderId,
            @RequestBody String deliveryAddress,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Updating delivery address for order: {}", orderId);

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            OrderResponse orderResponse = orderService.updateOrderDeliveryAddress(orderId, deliveryAddress, userId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDER_UPDATED_SUCCESS, orderResponse));

        } catch (RuntimeException e) {
            logger.error("Error updating delivery address for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error updating delivery address for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Cancel an order
     * @param orderId the ID of the order
     * @param authHeader the authorization header containing JWT token
     * @return the updated order response
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID orderId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Cancelling order: {}", orderId);

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            OrderResponse orderResponse = orderService.cancelOrder(orderId, userId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.ORDER_CANCELLED_SUCCESS, orderResponse));

        } catch (RuntimeException e) {
            logger.error("Error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error cancelling order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get order statistics for authenticated user
     * @param authHeader the authorization header containing JWT token
     * @return order statistics
     */
    @GetMapping("/my-statistics")
    public ResponseEntity<ApiResponse<OrderService.OrderStatistics>> getMyOrderStatistics(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Retrieving order statistics for authenticated user");

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            OrderService.OrderStatistics statistics = orderService.getOrderStatisticsByUserId(userId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.STATISTICS_RETRIEVED_SUCCESS, statistics));

        } catch (Exception e) {
            logger.error("Error retrieving user order statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Get order statistics for a plat
     * @param platId the ID of the plat
     * @return order statistics
     */
    @GetMapping("/plat/{platId}/statistics")
    public ResponseEntity<ApiResponse<OrderService.OrderStatistics>> getPlatOrderStatistics(@PathVariable UUID platId) {
        try {
            logger.info("Retrieving order statistics for plat: {}", platId);

            OrderService.OrderStatistics statistics = orderService.getOrderStatisticsByPlatId(platId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.STATISTICS_RETRIEVED_SUCCESS, statistics));

        } catch (Exception e) {
            logger.error("Error retrieving plat order statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }

    /**
     * Check if user has pending orders for a specific plat
     * @param platId the ID of the plat
     * @param authHeader the authorization header containing JWT token
     * @return true if user has pending orders
     */
    @GetMapping("/plat/{platId}/has-pending")
    public ResponseEntity<ApiResponse<Boolean>> hasPendingOrdersForPlat(
            @PathVariable UUID platId,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            logger.info("Checking pending orders for plat: {}", platId);

            // Extract user ID from JWT token
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            UUID userId = jwtService.extractUserId(token);

            boolean hasPending = orderService.hasPendingOrdersForPlat(userId, platId);

            return ResponseEntity.ok(ApiResponse.success(AppConstants.CHECK_COMPLETED_SUCCESS, hasPending));

        } catch (Exception e) {
            logger.error("Error checking pending orders for plat {}: {}", platId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError(AppConstants.INTERNAL_ERROR));
        }
    }
}