package com.dineo_backend.dineo.plats.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for updating an existing plat
 * All fields are optional - only provided fields will be updated
 */
public class UpdatePlatRequest {

    @Size(max = 200, message = "Le nom du plat ne peut pas dépasser 200 caractères")
    private String name;

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @Positive(message = "Le temps de cuisson doit être positif")
    private Integer estimatedCookTime; // in minutes

    @Positive(message = "Le prix doit être positif")
    private Double price; // Price in euros

    private List<String> categories;

    private Boolean available; // Whether the plat is available for order

    // Default constructor
    public UpdatePlatRequest() {}

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEstimatedCookTime() {
        return estimatedCookTime;
    }

    public void setEstimatedCookTime(Integer estimatedCookTime) {
        this.estimatedCookTime = estimatedCookTime;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "UpdatePlatRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedCookTime=" + estimatedCookTime +
                ", price=" + price +
                ", categories=" + categories +
                ", available=" + available +
                '}';
    }
}
