package com.dineo_backend.dineo.notifications.service;

import java.util.Map;

/**
 * Service interface for sending push notifications via Expo Push API.
 * This service handles sending push notifications to mobile devices
 * when the app is in the background or closed.
 */
public interface ExpoPushService {
    
    /**
     * Sends a push notification to a specific Expo push token.
     * 
     * @param pushToken The Expo push token (e.g., "ExponentPushToken[xxx]")
     * @param title The notification title
     * @param body The notification body message
     * @param data Additional data to include in the notification
     * @return true if the notification was sent successfully, false otherwise
     */
    boolean sendPushNotification(String pushToken, String title, String body, Map<String, Object> data);
    
    /**
     * Sends a push notification to a specific Expo push token without additional data.
     * 
     * @param pushToken The Expo push token
     * @param title The notification title
     * @param body The notification body message
     * @return true if the notification was sent successfully, false otherwise
     */
    boolean sendPushNotification(String pushToken, String title, String body);
}
