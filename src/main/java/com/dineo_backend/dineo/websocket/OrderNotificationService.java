package com.dineo_backend.dineo.websocket;

import com.dineo_backend.dineo.orders.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending real-time order notifications via WebSocket
 * Handles notifications to chefs and users about order status changes
 */
@Service
public class OrderNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to a specific chef about a new order
     * @param chefId the ID of the chef
     * @param order the order details
     */
    public void notifyChefNewOrder(UUID chefId, OrderResponse order) {
        try {
            logger.info("Sending new order notification to chef: {}", chefId);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_ORDER");
            notification.put("message", "Nouvelle commande");
            notification.put("order", order);
            
            // Send to specific chef's queue
            messagingTemplate.convertAndSend("/topic/chef/" + chefId + "/orders", notification);
            
            logger.info("New order notification sent successfully to chef: {}", chefId);
        } catch (Exception e) {
            logger.error("Error sending new order notification to chef {}: {}", chefId, e.getMessage());
        }
    }

    /**
     * Send notification to a user about their order status change
     * @param userId the ID of the user
     * @param order the updated order details
     * @param message the notification message
     */
    public void notifyUserOrderStatusChange(UUID userId, OrderResponse order, String message) {
        try {
            logger.info("Sending order status update to user: {}", userId);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ORDER_STATUS_UPDATE");
            notification.put("message", message);
            notification.put("order", order);
            
            // Send to specific user's queue
            messagingTemplate.convertAndSend("/topic/user/" + userId + "/orders", notification);
            
            logger.info("Order status notification sent successfully to user: {}", userId);
        } catch (Exception e) {
            logger.error("Error sending order status notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send notification when chef accepts an order
     * @param userId the ID of the user who placed the order
     * @param order the accepted order details
     */
    public void notifyUserOrderAccepted(UUID userId, OrderResponse order) {
        String message = "Votre commande a été acceptée par le chef";
        notifyUserOrderStatusChange(userId, order, message);
    }

    /**
     * Send notification when chef rejects an order
     * @param userId the ID of the user who placed the order
     * @param order the rejected order details
     */
    public void notifyUserOrderRejected(UUID userId, OrderResponse order) {
        String message = "Le chef ne peut pas accepter cette commande";
        notifyUserOrderStatusChange(userId, order, message);
    }

    /**
     * Send notification when order status changes (PREPARING, READY, COMPLETED)
     * @param userId the ID of the user who placed the order
     * @param order the updated order details
     */
    public void notifyUserOrderProgress(UUID userId, OrderResponse order) {
        String message = switch (order.getStatus()) {
            case PREPARING -> "Votre commande est en cours de préparation";
            case READY -> "Votre commande est prête pour la livraison";
            case COMPLETED -> "Votre commande a été livrée. Bon appétit !";
            default -> "Statut de votre commande : " + order.getStatus().getLabel();
        };
        notifyUserOrderStatusChange(userId, order, message);
    }

    /**
     * Send notification when user cancels an order
     * @param chefId the ID of the chef
     * @param order the cancelled order details
     */
    public void notifyChefOrderCancelled(UUID chefId, OrderResponse order) {
        try {
            logger.info("Sending order cancellation notification to chef: {}", chefId);
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "ORDER_CANCELLED");
            notification.put("message", "Commande annulée par le client");
            notification.put("order", order);
            
            messagingTemplate.convertAndSend("/topic/chef/" + chefId + "/orders", notification);
            
            logger.info("Order cancellation notification sent successfully to chef: {}", chefId);
        } catch (Exception e) {
            logger.error("Error sending order cancellation notification to chef {}: {}", chefId, e.getMessage());
        }
    }

    /**
     * Broadcast notification to all online chefs (for pending orders)
     * @param order the order details
     */
    public void broadcastNewOrderToAllChefs(OrderResponse order) {
        try {
            logger.info("Broadcasting new order to all chefs");
            
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_ORDER_BROADCAST");
            notification.put("message", "Nouvelle commande disponible");
            notification.put("order", order);
            
            // Broadcast to all chefs
            messagingTemplate.convertAndSend("/topic/chefs/orders", notification);
            
            logger.info("New order broadcast sent successfully to all chefs");
        } catch (Exception e) {
            logger.error("Error broadcasting new order to all chefs: {}", e.getMessage());
        }
    }
}
