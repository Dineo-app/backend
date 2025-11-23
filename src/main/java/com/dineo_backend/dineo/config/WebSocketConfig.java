package com.dineo_backend.dineo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time order notifications
 * Enables STOMP messaging over WebSocket for chef and user notifications
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for pub/sub messaging
     * - /topic: broadcasts to all subscribers
     * - /queue: point-to-point messaging
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for /topic and /queue destinations
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user-specific destination prefix for targeted messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register STOMP endpoints that clients will connect to
     * Enables SockJS fallback for browsers that don't support WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // Allow all origins for development
                .withSockJS(); // Enable SockJS fallback
        
        // Register endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }
}
