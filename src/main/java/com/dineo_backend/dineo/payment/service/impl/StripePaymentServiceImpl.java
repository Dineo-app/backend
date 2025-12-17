package com.dineo_backend.dineo.payment.service.impl;

import com.dineo_backend.dineo.payment.dto.CreatePaymentIntentRequest;
import com.dineo_backend.dineo.payment.dto.PaymentIntentResponse;
import com.dineo_backend.dineo.payment.service.StripePaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripePaymentServiceImpl implements StripePaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(StripePaymentServiceImpl.class);
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    
    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;
    
    @Value("${stripe.currency:eur}")
    private String defaultCurrency;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe API initialized successfully");
    }
    
    @Override
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        try {
            // Convert amount to cents (Stripe requires smallest currency unit)
            long amountInCents = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();
            
            // Build metadata from order items
            Map<String, String> metadata = new HashMap<>();
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                metadata.put("order_items_count", String.valueOf(request.getItems().size()));
                for (int i = 0; i < request.getItems().size(); i++) {
                    CreatePaymentIntentRequest.OrderItem item = request.getItems().get(i);
                    metadata.put("item_" + i + "_plat_id", item.getPlatId().toString());
                    metadata.put("item_" + i + "_plat_name", item.getPlatName());
                    metadata.put("item_" + i + "_quantity", item.getQuantity().toString());
                    metadata.put("item_" + i + "_chef_id", item.getChefId().toString());
                }
            }
            
            // Create PaymentIntent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency() != null ? request.getCurrency() : defaultCurrency)
                    .setDescription(request.getDescription() != null ? request.getDescription() : "Commande Miamlo")
                    .putAllMetadata(metadata)
                    // Enable automatic payment methods
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();
            
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            
            logger.info("Payment intent created successfully: {} for amount: {}", 
                       paymentIntent.getId(), amountInCents);
            
            return new PaymentIntentResponse(
                    paymentIntent.getClientSecret(),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    paymentIntent.getAmount(),
                    paymentIntent.getCurrency()
            );
            
        } catch (StripeException e) {
            logger.error("Error creating payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la création du paiement: " + e.getMessage());
        }
    }
    
    @Override
    public String confirmPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            logger.info("Payment intent {} status: {}", paymentIntentId, paymentIntent.getStatus());
            return paymentIntent.getStatus();
        } catch (StripeException e) {
            logger.error("Error confirming payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la vérification du paiement: " + e.getMessage());
        }
    }
    
    @Override
    public String getPublishableKey() {
        return stripePublishableKey;
    }
}
