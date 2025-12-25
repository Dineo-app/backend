package com.dineo_backend.dineo.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * DTO for adding an item to the cart
 */
public class AddToCartRequest {

    @NotNull(message = "L'ID du plat est obligatoire")
    private UUID platId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    private Integer quantity = 1;

    private List<UUID> selectedIngredientIds;

    // Constructors
    public AddToCartRequest() {
    }

    public AddToCartRequest(UUID platId, Integer quantity) {
        this.platId = platId;
        this.quantity = quantity;
    }

    public AddToCartRequest(UUID platId, Integer quantity, List<UUID> selectedIngredientIds) {
        this.platId = platId;
        this.quantity = quantity;
        this.selectedIngredientIds = selectedIngredientIds;
    }

    // Getters and Setters
    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<UUID> getSelectedIngredientIds() {
        return selectedIngredientIds;
    }

    public void setSelectedIngredientIds(List<UUID> selectedIngredientIds) {
        this.selectedIngredientIds = selectedIngredientIds;
    }
}
