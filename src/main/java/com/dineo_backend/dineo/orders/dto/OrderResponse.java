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
    private String description;
    private OrderStatus status;
    private String statusLabel;
    private String chefNotes;
    private String deliveryAddress;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private Double totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public OrderResponse() {}

    // Constructor with all fields
    public OrderResponse(UUID id, UUID platId, UUID userId, String description, 
                        OrderStatus status, String chefNotes, String deliveryAddress,
                        LocalDateTime estimatedDeliveryTime, LocalDateTime actualDeliveryTime,
                        Double totalPrice, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.platId = platId;
        this.userId = userId;
        this.description = description;
        this.status = status;
        this.statusLabel = status != null ? status.getLabel() : null;
        this.chefNotes = chefNotes;
        this.deliveryAddress = deliveryAddress;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
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

    public String getChefNotes() {
        return chefNotes;
    }

    public void setChefNotes(String chefNotes) {
        this.chefNotes = chefNotes;
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

    public LocalDateTime getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
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
                ", description='" + description + '\'' +
                ", status=" + status +
                ", statusLabel='" + statusLabel + '\'' +
                ", chefNotes='" + chefNotes + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", estimatedDeliveryTime=" + estimatedDeliveryTime +
                ", actualDeliveryTime=" + actualDeliveryTime +
                ", totalPrice=" + totalPrice +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}