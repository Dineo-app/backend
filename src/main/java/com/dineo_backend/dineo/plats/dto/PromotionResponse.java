package com.dineo_backend.dineo.plats.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for promotion information
 */
public class PromotionResponse {

    private UUID id;
    private UUID platId;
    private BigDecimal reductionValue;
    private LocalDateTime reductionEnds;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public PromotionResponse() {}

    public PromotionResponse(UUID id, UUID platId, BigDecimal reductionValue, 
                            LocalDateTime reductionEnds, Boolean isActive, 
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
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

    @Override
    public String toString() {
        return "PromotionResponse{" +
                "id=" + id +
                ", platId=" + platId +
                ", reductionValue=" + reductionValue +
                ", reductionEnds=" + reductionEnds +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
