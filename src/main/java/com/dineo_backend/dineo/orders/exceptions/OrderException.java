package com.dineo_backend.dineo.orders.exceptions;

/**
 * Custom exception for order-related operations
 * Thrown when order business logic violations occur
 */
public class OrderException extends RuntimeException {

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}