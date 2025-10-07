package com.dineo_backend.dineo.chefs.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's favorite chef
 * Tracks which users have favorited which chefs
 */
@Entity
@Table(name = "chef_favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"chef_id", "user_id"}))
public class ChefFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "chef_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID chefId;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor
    public ChefFavorite() {}

    // Constructor with required fields
    public ChefFavorite(UUID chefId, UUID userId) {
        this.chefId = chefId;
        this.userId = userId;
    }

    // Constructor with all fields
    public ChefFavorite(UUID id, UUID chefId, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.chefId = chefId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    // toString method
    @Override
    public String toString() {
        return "ChefFavorite{" +
                "id=" + id +
                ", chefId=" + chefId +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChefFavorite that = (ChefFavorite) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (chefId != null ? !chefId.equals(that.chefId) : that.chefId != null) return false;
        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (chefId != null ? chefId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}