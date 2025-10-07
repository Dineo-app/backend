package com.dineo_backend.dineo.plats.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for creating a new plat
 * Used for receiving plat creation requests from chefs
 */
public class CreatePlatRequest {

    @NotBlank(message = "Le nom du plat est obligatoire")
    @Size(max = 200, message = "Le nom du plat ne peut pas dépasser 200 caractères")
    private String name;

    @NotBlank(message = "La description du plat est obligatoire")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "Le temps de cuisson estimé est obligatoire")
    @Positive(message = "Le temps de cuisson doit être positif")
    private Integer estimatedCookTime; // in minutes

    @NotNull(message = "Les catégories sont obligatoires")
    @Size(min = 1, message = "Au moins une catégorie est requise")
    private List<String> categories;

    // Default constructor
    public CreatePlatRequest() {}

    // Constructor with all fields
    public CreatePlatRequest(String name, String description, Integer estimatedCookTime, List<String> categories) {
        this.name = name;
        this.description = description;
        this.estimatedCookTime = estimatedCookTime;
        this.categories = categories;
    }

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

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "CreatePlatRequest{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedCookTime=" + estimatedCookTime +
                ", categories=" + categories +
                '}';
    }
}