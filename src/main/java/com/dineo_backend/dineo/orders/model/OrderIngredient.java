package com.dineo_backend.dineo.orders.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Entity representing selected ingredients for an order
 * Stores which ingredients were selected (kept or added) by the user
 */
@Entity
@Table(name = "order_ingredients")
public class OrderIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @NotNull
    @Column(name = "ingredient_id", nullable = false)
    private UUID ingredientId;

    @NotNull
    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @NotNull
    @Column(name = "ingredient_price", nullable = false)
    private Double ingredientPrice;

    @NotNull
    @Column(name = "is_free", nullable = false)
    private Boolean isFree;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Default constructor
    public OrderIngredient() {
    }

    // Constructor with all fields
    public OrderIngredient(UUID orderId, UUID ingredientId, String ingredientName, Double ingredientPrice, Boolean isFree, Integer quantity) {
        this.orderId = orderId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.ingredientPrice = ingredientPrice;
        this.isFree = isFree;
        this.quantity = quantity;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(UUID ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public Double getIngredientPrice() {
        return ingredientPrice;
    }

    public void setIngredientPrice(Double ingredientPrice) {
        this.ingredientPrice = ingredientPrice;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderIngredient{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", ingredientId=" + ingredientId +
                ", ingredientName='" + ingredientName + '\'' +
                ", ingredientPrice=" + ingredientPrice +
                ", isFree=" + isFree +
                ", quantity=" + quantity +
                '}';
    }
}
