package com.dineo_backend.dineo.cart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Entity representing an ingredient selected for a cart item
 */
@Entity
@Table(name = "cart_item_ingredients")
public class CartItemIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "cart_item_id", nullable = false)
    private UUID cartItemId;

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

    // Constructors
    public CartItemIngredient() {
    }

    public CartItemIngredient(UUID cartItemId, UUID ingredientId, String ingredientName, 
                             Double ingredientPrice, Boolean isFree) {
        this.cartItemId = cartItemId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.ingredientPrice = ingredientPrice;
        this.isFree = isFree;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(UUID cartItemId) {
        this.cartItemId = cartItemId;
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
}
