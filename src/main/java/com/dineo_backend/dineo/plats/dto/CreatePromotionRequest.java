package com.dineo_backend.dineo.plats.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a new promotion
 */
public class CreatePromotionRequest {

    @NotNull(message = "L'ID du plat est requis")
    private UUID platId;

    @NotNull(message = "La valeur de réduction est requise")
    @DecimalMin(value = "0.01", message = "La réduction doit être au moins de 0.01%")
    @DecimalMax(value = "99.99", message = "La réduction ne peut pas dépasser 99.99%")
    private BigDecimal reductionValue;

    @NotNull(message = "La date de fin est requise")
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDateTime reductionEnds;

    // Constructors
    public CreatePromotionRequest() {}

    public CreatePromotionRequest(UUID platId, BigDecimal reductionValue, LocalDateTime reductionEnds) {
        this.platId = platId;
        this.reductionValue = reductionValue;
        this.reductionEnds = reductionEnds;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "CreatePromotionRequest{" +
                "platId=" + platId +
                ", reductionValue=" + reductionValue +
                ", reductionEnds=" + reductionEnds +
                '}';
    }
}
