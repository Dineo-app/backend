package com.dineo_backend.dineo.chefs.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a chef's description profile
 * Contains chef's bio, specialties, and certifications
 */
@Entity
@Table(name = "chef_descriptions")
public class ChefDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(
        name = "chef_categories", 
        joinColumns = @JoinColumn(name = "chef_description_id")
    )
    @Column(name = "category")
    private List<String> categories;

    @ElementCollection
    @CollectionTable(
        name = "chef_certifications", 
        joinColumns = @JoinColumn(name = "chef_description_id")
    )
    @Column(name = "certification")
    private List<String> chefCertifications;

    @Size(max = 500)
    @Column(name = "chef_cover_img")
    private String chefCoverImg;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public ChefDescription() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public ChefDescription(UUID userId, String description, List<String> categories, List<String> chefCertifications) {
        this();
        this.userId = userId;
        this.description = description;
        this.categories = categories;
        this.chefCertifications = chefCertifications;
    }

    // Constructor with all fields including chefCoverImg
    public ChefDescription(UUID userId, String description, List<String> categories, 
                          List<String> chefCertifications, String chefCoverImg) {
        this();
        this.userId = userId;
        this.description = description;
        this.categories = categories;
        this.chefCertifications = chefCertifications;
        this.chefCoverImg = chefCoverImg;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getChefCertifications() {
        return chefCertifications;
    }

    public void setChefCertifications(List<String> chefCertifications) {
        this.chefCertifications = chefCertifications;
    }

    public String getChefCoverImg() {
        return chefCoverImg;
    }

    public void setChefCoverImg(String chefCoverImg) {
        this.chefCoverImg = chefCoverImg;
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
        return "ChefDescription{" +
                "id=" + id +
                ", userId=" + userId +
                ", description='" + description + '\'' +
                ", categories=" + categories +
                ", chefCertifications=" + chefCertifications +
                ", chefCoverImg='" + chefCoverImg + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}