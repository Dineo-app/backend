package com.dineo_backend.dineo.chefs.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for adding a chef to favorites
 */
public class AddFavoriteChefRequest {

    @NotNull(message = "L'ID du chef est obligatoire")
    private UUID chefId;

    // Default constructor
    public AddFavoriteChefRequest() {}

    // Constructor with chefId
    public AddFavoriteChefRequest(UUID chefId) {
        this.chefId = chefId;
    }

    // Getters and Setters
    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

    @Override
    public String toString() {
        return "AddFavoriteChefRequest{" +
                "chefId=" + chefId +
                '}';
    }
}