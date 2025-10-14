package com.dineo_backend.dineo.plats.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a dish/plate created by a chef
 * Contains dish details, cooking information, and categorization
 */
@Entity
@Table(name = "plats")
public class Plat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "chef_id", nullable = false)
    private UUID chefId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "estimated_cook_time", nullable = false)
    private Integer estimatedCookTime; // in minutes

    @NotNull
    @Column(name = "price", nullable = false)
    private Double price; // Price in euros

    @ElementCollection
    @CollectionTable(
        name = "plat_categories", 
        joinColumns = @JoinColumn(name = "plat_id")
    )
    @Column(name = "category")
    private List<String> categories;

    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Plat() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public Plat(UUID chefId, String name, String description, Integer estimatedCookTime, Double price, List<String> categories) {
        this();
        this.chefId = chefId;
        this.name = name;
        this.description = description;
        this.estimatedCookTime = estimatedCookTime;
        this.price = price;
        this.categories = categories;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Plat{" +
                "id=" + id +
                ", chefId=" + chefId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedCookTime=" + estimatedCookTime +
                ", price=" + price +
                ", categories=" + categories +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}