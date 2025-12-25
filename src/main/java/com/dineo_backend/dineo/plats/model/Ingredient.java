package com.dineo_backend.dineo.plats.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Entity representing an ingredient for a dish/plate
 * Ingredients can be free (default) or paid (add-on)
 */
@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "plat_id", nullable = false)
    private UUID platId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "price", nullable = false)
    private Double price; // Price in euros (0.0 for free ingredients)

    @NotNull
    @Column(name = "is_free", nullable = false)
    private Boolean isFree; // true for free (default), false for paid

    // Default constructor
    public Ingredient() {
    }

    // Constructor with required fields
    public Ingredient(UUID platId, String name, Double price, Boolean isFree) {
        this.platId = platId;
        this.name = name;
        this.price = price;
        this.isFree = isFree;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", platId=" + platId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", isFree=" + isFree +
                '}';
    }
}
