package com.dineo_backend.dineo.plats.dto;

import java.util.UUID;

/**
 * DTO for ingredient response
 */
public class IngredientResponse {

    private UUID id;
    private UUID platId;
    private String name;
    private Double price;
    private Boolean isFree;

    // Constructors
    public IngredientResponse() {
    }

    public IngredientResponse(UUID id, UUID platId, String name, Double price, Boolean isFree) {
        this.id = id;
        this.platId = platId;
        this.name = name;
        this.price = price;
        this.isFree = isFree;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }
}
