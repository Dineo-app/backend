package com.dineo_backend.dineo.chefs.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for favorite chef response with complete chef details
 * Includes both favorite information and full chef details
 */
public class FavoriteChefWithDetailsResponse {

    // Favorite information
    private UUID favoriteId;
    private LocalDateTime favoritedAt;

    // Chef user information
    private UUID chefId;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    // Chef description information
    private UUID chefDescriptionId;
    private String description;
    private List<String> categories;
    private List<String> chefCertifications;
    private String chefCoverImg;
    private LocalDateTime chefCreatedAt;
    private LocalDateTime chefUpdatedAt;

    // Default constructor
    public FavoriteChefWithDetailsResponse() {}

    // Constructor with all fields
    public FavoriteChefWithDetailsResponse(UUID favoriteId, LocalDateTime favoritedAt,
                                         UUID chefId, String firstName, String lastName,
                                         String phone, String address, UUID chefDescriptionId,
                                         String description, List<String> categories,
                                         List<String> chefCertifications, String chefCoverImg,
                                         LocalDateTime chefCreatedAt, LocalDateTime chefUpdatedAt) {
        this.favoriteId = favoriteId;
        this.favoritedAt = favoritedAt;
        this.chefId = chefId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
        this.chefDescriptionId = chefDescriptionId;
        this.description = description;
        this.categories = categories;
        this.chefCertifications = chefCertifications;
        this.chefCoverImg = chefCoverImg;
        this.chefCreatedAt = chefCreatedAt;
        this.chefUpdatedAt = chefUpdatedAt;
    }

    // Getters and Setters
    public UUID getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(UUID favoriteId) {
        this.favoriteId = favoriteId;
    }

    public LocalDateTime getFavoritedAt() {
        return favoritedAt;
    }

    public void setFavoritedAt(LocalDateTime favoritedAt) {
        this.favoritedAt = favoritedAt;
    }

    public UUID getChefId() {
        return chefId;
    }

    public void setChefId(UUID chefId) {
        this.chefId = chefId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UUID getChefDescriptionId() {
        return chefDescriptionId;
    }

    public void setChefDescriptionId(UUID chefDescriptionId) {
        this.chefDescriptionId = chefDescriptionId;
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

    public LocalDateTime getChefCreatedAt() {
        return chefCreatedAt;
    }

    public void setChefCreatedAt(LocalDateTime chefCreatedAt) {
        this.chefCreatedAt = chefCreatedAt;
    }

    public LocalDateTime getChefUpdatedAt() {
        return chefUpdatedAt;
    }

    public void setChefUpdatedAt(LocalDateTime chefUpdatedAt) {
        this.chefUpdatedAt = chefUpdatedAt;
    }

    @Override
    public String toString() {
        return "FavoriteChefWithDetailsResponse{" +
                "favoriteId=" + favoriteId +
                ", favoritedAt=" + favoritedAt +
                ", chefId=" + chefId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", chefDescriptionId=" + chefDescriptionId +
                ", description='" + description + '\'' +
                ", categories=" + categories +
                ", chefCertifications=" + chefCertifications +
                ", chefCoverImg='" + chefCoverImg + '\'' +
                ", chefCreatedAt=" + chefCreatedAt +
                ", chefUpdatedAt=" + chefUpdatedAt +
                '}';
    }
}