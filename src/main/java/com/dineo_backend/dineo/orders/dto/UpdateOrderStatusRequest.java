package com.dineo_backend.dineo.orders.dto;

import com.dineo_backend.dineo.orders.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for updating order status
 * Used by chefs and administrators to update order status
 */
public class UpdateOrderStatusRequest {

    @NotNull(message = "Le statut de la commande est obligatoire")
    private OrderStatus status;

    private String orderNotes;

    private LocalDateTime estimatedDeliveryTime;

    private Double totalPrice;

    // Default constructor
    public UpdateOrderStatusRequest() {}

    // Constructor with required fields
    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    // Constructor with all fields
    public UpdateOrderStatusRequest(OrderStatus status, String orderNotes, 
                                   LocalDateTime estimatedDeliveryTime, Double totalPrice) {
        this.status = status;
        this.orderNotes = orderNotes;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public LocalDateTime getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(LocalDateTime estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequest{" +
                "status=" + status +
                ", orderNotes='" + orderNotes + '\'' +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", totalPrice=" + totalPrice +
                '}';
    }
}