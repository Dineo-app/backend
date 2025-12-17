package com.dineo_backend.dineo.payment.service;

import com.dineo_backend.dineo.payment.dto.CreatePaymentIntentRequest;
import com.dineo_backend.dineo.payment.dto.PaymentIntentResponse;

public interface StripePaymentService {
    
    /**
     * Create a payment intent for Stripe checkout
     * @param request Payment details
     * @return Payment intent with client secret
     */
    PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);
    
    /**
     * Confirm payment intent status
     * @param paymentIntentId Payment intent ID from Stripe
     * @return Payment status
     */
    String confirmPaymentIntent(String paymentIntentId);
    
    /**
     * Get Stripe publishable key for frontend
     * @return Publishable key
     */
    String getPublishableKey();
}
