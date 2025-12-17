package com.dineo_backend.dineo.payment.controller;

import com.dineo_backend.dineo.payment.dto.CreatePaymentIntentRequest;
import com.dineo_backend.dineo.payment.dto.PaymentIntentResponse;
import com.dineo_backend.dineo.payment.service.StripePaymentService;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private StripePaymentService stripePaymentService;
    
    /**
     * Get Stripe publishable key for frontend
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, String>>> getConfig() {
        try {
            String publishableKey = stripePaymentService.getPublishableKey();
            
            Map<String, String> config = Map.of(
                    "publishableKey", publishableKey
            );
            
            return ResponseEntity.ok(ApiResponse.success("Configuration retrieved successfully", config));
        } catch (Exception e) {
            logger.error("Error getting payment config: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error retrieving payment configuration"));
        }
    }
    
    /**
     * Create payment intent
     */
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<PaymentIntentResponse>> createPaymentIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request) {
        try {
            logger.info("Creating payment intent for amount: {}", request.getAmount());
            
            PaymentIntentResponse response = stripePaymentService.createPaymentIntent(request);
            
            return ResponseEntity.ok(
                    ApiResponse.success("Payment intent created successfully", response)
            );
        } catch (RuntimeException e) {
            logger.error("Error creating payment intent: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating payment intent: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }
    
    /**
     * Confirm payment status
     */
    @GetMapping("/confirm/{paymentIntentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('PROVIDER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> confirmPayment(
            @PathVariable String paymentIntentId) {
        try {
            logger.info("Confirming payment intent: {}", paymentIntentId);
            
            String status = stripePaymentService.confirmPaymentIntent(paymentIntentId);
            
            Map<String, String> response = Map.of(
                    "paymentIntentId", paymentIntentId,
                    "status", status
            );
            
            return ResponseEntity.ok(
                    ApiResponse.success("Payment status retrieved successfully", response)
            );
        } catch (RuntimeException e) {
            logger.error("Error confirming payment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error confirming payment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }
}
