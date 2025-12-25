package com.dineo_backend.dineo.plats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating/updating an ingredient
 */
public class IngredientRequest {

    @NotBlank(message = "Ingredient name is required")
    @Size(max = 100, message = "Ingredient name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Price is required")
    private Double price; // 0.0 for free ingredients

    @NotNull(message = "isFree field is required")
    private Boolean isFree;

    // Constructors
    public IngredientRequest() {
    }

    public IngredientRequest(String name, Double price, Boolean isFree) {
        this.name = name;
        this.price = price;
        this.isFree = isFree;
    }

    // Getters and Setters
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
