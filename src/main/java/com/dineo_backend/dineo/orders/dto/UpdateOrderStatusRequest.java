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

    private String chefNotes;

    private LocalDateTime estimatedDeliveryTime;

    private Double totalPrice;

    // Default constructor
    public UpdateOrderStatusRequest() {}

    // Constructor with required fields
    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    // Constructor with all fields
    public UpdateOrderStatusRequest(OrderStatus status, String chefNotes, 
                                   LocalDateTime estimatedDeliveryTime, Double totalPrice) {
        this.status = status;
        this.chefNotes = chefNotes;
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

    public String getChefNotes() {
        return chefNotes;
    }

    public void setChefNotes(String chefNotes) {
        this.chefNotes = chefNotes;
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
                ", chefNotes='" + chefNotes + '\'' +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", totalPrice=" + totalPrice +
                '}';
    }
}