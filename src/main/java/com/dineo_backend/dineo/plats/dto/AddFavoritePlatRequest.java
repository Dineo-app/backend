package com.dineo_backend.dineo.plats.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for adding a plat to favorites
 */
public class AddFavoritePlatRequest {

    @NotNull(message = "L'ID du plat est obligatoire")
    private UUID platId;

    // Default constructor
    public AddFavoritePlatRequest() {}

    // Constructor with platId
    public AddFavoritePlatRequest(UUID platId) {
        this.platId = platId;
    }

    // Getters and Setters
    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
    }

    @Override
    public String toString() {
        return "AddFavoritePlatRequest{" +
                "platId=" + platId +
                '}';
    }
}