package com.dineo_backend.dineo.administration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for admin to create a plate for a chef
 */
public class CreatePlateForChefRequest {
    
    @NotBlank(message = "Plate name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;
    
    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Estimated cook time is required")
    private Integer estimatedCookTime; // in minutes
    
    @NotNull(message = "Price is required")
    private Double price;
    
    private List<String> categories;
    
    private String imageUrl;
    
    private Boolean available = true;

    public CreatePlateForChefRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getEstimatedCookTime() { return estimatedCookTime; }
    public void setEstimatedCookTime(Integer estimatedCookTime) { this.estimatedCookTime = estimatedCookTime; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
}
