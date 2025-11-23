package com.dineo_backend.dineo.orders.dto;

import com.dineo_backend.dineo.orders.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for order response
 * Contains all order information for API responses
 */
public class OrderResponse {

    private UUID id;
    private UUID platId;
    private UUID userId;
    private UUID chefId;
    private String description;
    private OrderStatus status;
    private String statusLabel;
    private Integer quantity;
    private String orderNotes;
    private String deliveryAddress;
    private LocalDateTime estimatedDeliveryTime;
    private Double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public OrderResponse() {}

    // Constructor with all fields
    public OrderResponse(UUID id, UUID platId, UUID userId, UUID chefId, String description, 
                        OrderStatus status, Integer quantity, String orderNotes, String deliveryAddress,
                        LocalDateTime estimatedDeliveryTime, Double totalPrice, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.platId = platId;
        this.userId = userId;
        this.chefId = chefId;
        this.description = description;
        this.status = status;
        this.statusLabel = status != null ? status.getLabel() : null;
        this.quantity = quantity;
        this.orderNotes = orderNotes;
        this.deliveryAddress = deliveryAddress;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.statusLabel = status != null ? status.getLabel() : null;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id=" + id +
                ", platId=" + platId +
                ", userId=" + userId +
                ", chefId=" + chefId +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", statusLabel='" + statusLabel + '\'' +
                ", quantity=" + quantity +
                ", orderNotes='" + orderNotes + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}