package com.dineo_backend.dineo.plats.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a user's favorite plat
 * Tracks which users have favorited which plats
 */
@Entity
@Table(name = "favorite_plats", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"plat_id", "user_id"}))
public class FavoritePlat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "plat_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID platId;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor
    public FavoritePlat() {}

    // Constructor with required fields
    public FavoritePlat(UUID platId, UUID userId) {
        this.platId = platId;
        this.userId = userId;
    }

    // Constructor with all fields
    public FavoritePlat(UUID id, UUID platId, UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.platId = platId;
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

    public UUID getPlatId() {
        return platId;
    }

    public void setPlatId(UUID platId) {
        this.platId = platId;
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
        return "FavoritePlat{" +
                "id=" + id +
                ", platId=" + platId +
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

        FavoritePlat that = (FavoritePlat) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (platId != null ? !platId.equals(that.platId) : that.platId != null) return false;
        return userId != null ? userId.equals(that.userId) : that.userId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (platId != null ? platId.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}