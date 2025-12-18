package com.dineo_backend.dineo.orders.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for creating a new order
 * Contains necessary information for order creation
 */
public class CreateOrderRequest {

    @NotNull(message = "L'ID du plat est obligatoire")
    private UUID platId;

    @NotNull(message = "L'ID du chef est obligatoire")
    private UUID chefId;

    @NotNull(message = "La quantité est obligatoire")
    @jakarta.validation.constraints.Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity = 1;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    private String deliveryAddress;

    // Payment Intent ID for payment verification
    // TODO: Make @NotNull once all clients are updated to payment flow
    private String paymentIntentId;

    // Default constructor
    public CreateOrderRequest() {}

    // Constructor with required fields
    public CreateOrderRequest(UUID platId, UUID chefId, Integer quantity, String description) {
        this.platId = platId;
        this.chefId = chefId;
        this.quantity = quantity;
        this.description = description;
    }

    // Constructor with all fields
    public CreateOrderRequest(UUID platId, UUID chefId, Integer quantity, String description, String deliveryAddress) {
        this.platId = platId;
        this.chefId = chefId;
        this.quantity = quantity;
        this.description = description;
        this.deliveryAddress = deliveryAddress;
    }

    // Constructor with payment
    public CreateOrderRequest(UUID platId, UUID chefId, Integer quantity, String description, String deliveryAddress, String paymentIntentId) {
        this.platId = platId;
        this.chefId = chefId;
        this.quantity = quantity;
        this.description = description;
        this.deliveryAddress = deliveryAddress;
        this.paymentIntentId = paymentIntentId;
    }

    // Getters and Setters
    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
    }

    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "platId=" + platId +
                ", chefId=" + chefId +
                ", quantity=" + quantity +
                ", description='" + description + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", paymentIntentId='" + paymentIntentId + '\'' +
                '}';
    }
}