package com.dineo_backend.dineo.plats.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a promotion/reduction for a plat
 * Manages discount percentages and expiration dates for plats
 */
@Entity
@Table(name = "promotion_plats")
public class PromotionPlat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @NotNull
    @Column(name = "plat_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID platId;

    @NotNull
    @DecimalMin(value = "0.01", message = "La réduction doit être au moins de 0.01%")
    @DecimalMax(value = "99.99", message = "La réduction ne peut pas dépasser 99.99%")
    @Column(name = "reduction_value", nullable = false, precision = 5, scale = 2)
    private BigDecimal reductionValue; // Percentage (e.g., 15.50 for 15.5%)

    @NotNull
    @Column(name = "reduction_ends", nullable = false)
    private LocalDateTime reductionEnds;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor
    public PromotionPlat() {}

    // Constructor with required fields
    public PromotionPlat(UUID platId, BigDecimal reductionValue, LocalDateTime reductionEnds) {
        this.platId = platId;
        this.reductionValue = reductionValue;
        this.reductionEnds = reductionEnds;
        this.isActive = true;
    }

    // Constructor with all fields
    public PromotionPlat(UUID id, UUID platId, BigDecimal reductionValue, LocalDateTime reductionEnds, 
                        Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.platId = platId;
        this.reductionValue = reductionValue;
        this.reductionEnds = reductionEnds;
        this.isActive = isActive;
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

    public BigDecimal getReductionValue() {
        return reductionValue;
    }

    public void setReductionValue(BigDecimal reductionValue) {
        this.reductionValue = reductionValue;
    }

    public LocalDateTime getReductionEnds() {
        return reductionEnds;
    }

    public void setReductionEnds(LocalDateTime reductionEnds) {
        this.reductionEnds = reductionEnds;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    /**
     * Check if the promotion is currently active and not expired
     * 
     * @return true if promotion is active and not expired
     */
    public boolean isCurrentlyActive() {
        return this.isActive && this.reductionEnds != null && this.reductionEnds.isAfter(LocalDateTime.now());
    }

    /**
     * Check if the promotion has expired
     * 
     * @return true if promotion has expired
     */
    public boolean isExpired() {
        return this.reductionEnds != null && this.reductionEnds.isBefore(LocalDateTime.now());
    }

    // toString method
    @Override
    public String toString() {
        return "PromotionPlat{" +
                "id=" + id +
                ", platId=" + platId +
                ", reductionValue=" + reductionValue +
                ", reductionEnds=" + reductionEnds +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PromotionPlat that = (PromotionPlat) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (platId != null ? !platId.equals(that.platId) : that.platId != null) return false;
        return reductionValue != null ? reductionValue.equals(that.reductionValue) : that.reductionValue == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (platId != null ? platId.hashCode() : 0);
        result = 31 * result + (reductionValue != null ? reductionValue.hashCode() : 0);
        return result;
    }
}