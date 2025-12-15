package com.dineo_backend.dineo.websocket;

import com.dineo_backend.dineo.authentication.model.UserPushToken;
import com.dineo_backend.dineo.authentication.repository.UserPushTokenRepository;
import com.dineo_backend.dineo.notifications.service.ExpoPushService;
import com.dineo_backend.dineo.orders.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for sending real-time order notifications via WebSocket
 * and push notifications via Expo Push API (to ALL user devices)
 * Handles notifications to chefs and users about order status changes
 */
@Service
public class OrderNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(OrderNotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ExpoPushService expoPushService;
    
    @Autowired
    private UserPushTokenRepository pushTokenRepository;

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
            
            // Send to specific chef's queue via WebSocket
            messagingTemplate.convertAndSend("/topic/chef/" + chefId + "/orders", notification);
            
            logger.info("New order notification sent successfully to chef: {}", chefId);
            
            // Also send push notification
            sendPushNotificationToUser(chefId, "Nouvelle commande 🍽️", 
                "Vous avez reçu une nouvelle commande!", notification);
            
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
            
            // Send to specific user's queue via WebSocket
            messagingTemplate.convertAndSend("/topic/user/" + userId + "/orders", notification);
            
            logger.info("Order status notification sent successfully to user: {}", userId);
            
            // Also send push notification
            sendPushNotificationToUser(userId, "Mise à jour de commande 🍜", message, notification);
            
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
        String message = "Votre commande a été acceptée";
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
            
            // Also send push notification
            sendPushNotificationToUser(chefId, "Commande annulée ❌", 
                "Une commande a été annulée par le client", notification);
            
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
    
    /**
     * Helper method to send push notification to ALL devices of a user.
     * Supports multi-device - sends to every registered device.
     * 
     * @param userId the ID of the user
     * @param title the notification title
     * @param body the notification body
     * @param data additional data to include
     */
    private void sendPushNotificationToUser(UUID userId, String title, String body, Map<String, Object> data) {
        try {
            // Get all push tokens for this user (all devices)
            List<UserPushToken> userTokens = pushTokenRepository.findByUserId(userId);
            
            if (userTokens.isEmpty()) {
                logger.debug("User {} has no push tokens registered, skipping push notification", userId);
                return;
            }
            
            int successCount = 0;
            int failCount = 0;
            
            // Send to all devices
            for (UserPushToken tokenEntry : userTokens) {
                String pushToken = tokenEntry.getPushToken();
                boolean sent = expoPushService.sendPushNotification(pushToken, title, body, data);
                
                if (sent) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            logger.info("Push notifications sent to user {}: {} success, {} failed (total {} devices)", 
                userId, successCount, failCount, userTokens.size());
                
        } catch (Exception e) {
            logger.error("Error sending push notifications to user {}: {}", userId, e.getMessage());
        }
    }
}
