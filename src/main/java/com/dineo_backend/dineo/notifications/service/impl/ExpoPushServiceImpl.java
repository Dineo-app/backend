package com.dineo_backend.dineo.notifications.service.impl;

import com.dineo_backend.dineo.notifications.service.ExpoPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link ExpoPushService} for sending push notifications via Expo Push API.
 * This service calls the Expo Push API to deliver notifications to mobile devices
 * when the app is in the background or closed.
 * 
 * @see <a href="https://docs.expo.dev/push-notifications/sending-notifications/">Expo Push Notifications</a>
 */
@Service
public class ExpoPushServiceImpl implements ExpoPushService {

    private static final Logger logger = LoggerFactory.getLogger(ExpoPushServiceImpl.class);
    private static final String EXPO_PUSH_API_URL = "https://exp.host/--/api/v2/push/send";
    
    private final RestTemplate restTemplate;
    
    @Value("${expo.push.access-token:}")
    private String expoAccessToken;

    public ExpoPushServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public boolean sendPushNotification(String pushToken, String title, String body, Map<String, Object> data) {
        if (pushToken == null || pushToken.isEmpty()) {
            logger.warn("Cannot send push notification: pushToken is null or empty");
            return false;
        }
        
        if (!isValidExpoPushToken(pushToken)) {
            logger.warn("Invalid Expo push token format: {}", pushToken);
            return false;
        }
        
        try {
            // Build the notification payload
            Map<String, Object> message = new HashMap<>();
            message.put("to", pushToken);
            message.put("title", title);
            message.put("body", body);
            message.put("sound", "default");
            message.put("priority", "high");
            
            if (data != null && !data.isEmpty()) {
                message.put("data", data);
            }
            
            // Wrap in array as Expo API expects
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Accept-encoding", "gzip, deflate");
            
            // Add access token if configured (for high-volume sending)
            if (expoAccessToken != null && !expoAccessToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + expoAccessToken);
            }
            
            HttpEntity<List<Map<String, Object>>> requestEntity = new HttpEntity<>(messages, headers);
            
            // Send request to Expo Push API
            ResponseEntity<Map> response = restTemplate.postForEntity(EXPO_PUSH_API_URL, requestEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Push notification sent successfully to token: {}", 
                    pushToken.substring(0, Math.min(20, pushToken.length())) + "...");
                
                // Check for errors in response
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("data")) {
                    List<Map<String, Object>> responseData = (List<Map<String, Object>>) responseBody.get("data");
                    if (!responseData.isEmpty()) {
                        Map<String, Object> firstResult = responseData.get(0);
                        if ("error".equals(firstResult.get("status"))) {
                            String errorMessage = (String) firstResult.get("message");
                            logger.error("Expo Push API error: {}", errorMessage);
                            return false;
                        }
                    }
                }
                return true;
            } else {
                logger.error("Failed to send push notification. Status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error sending push notification to token: {}", 
                pushToken.substring(0, Math.min(20, pushToken.length())) + "...", e);
            return false;
        }
    }

    @Override
    public boolean sendPushNotification(String pushToken, String title, String body) {
        return sendPushNotification(pushToken, title, body, null);
    }
    
    /**
     * Validates if the token is a valid Expo push token format.
     * Expo push tokens start with "ExponentPushToken[" or "ExpoPushToken[".
     * 
     * @param token The token to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidExpoPushToken(String token) {
        return token != null && 
               (token.startsWith("ExponentPushToken[") || token.startsWith("ExpoPushToken["));
    }
}
