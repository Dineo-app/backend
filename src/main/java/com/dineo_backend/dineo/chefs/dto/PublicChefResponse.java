package com.dineo_backend.dineo.chefs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for public chef response with all chef information
 */
public class PublicChefResponse {
    
    @JsonProperty("id")
    private UUID id;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("chefCoverImg")
    private String chefCoverImg;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("address")
    private String address;
    
    @JsonProperty("categories")
    private List<String> categories;
    
    @JsonProperty("chefCertifications")
    private List<String> chefCertifications;
    
    @JsonProperty("averageRating")
    private Double averageRating;
    
    @JsonProperty("totalReviews")
    private Integer totalReviews;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("distanceKm")
    private Double distanceKm;
    
    // Default constructor
    public PublicChefResponse() {
    }
    
    // Constructor with all fields
    public PublicChefResponse(UUID id, String firstName, String lastName, String chefCoverImg, 
                             String description, String address, List<String> categories, 
                             List<String> chefCertifications, Double averageRating, 
                             Integer totalReviews, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.chefCoverImg = chefCoverImg;
        this.description = description;
        this.address = address;
        this.categories = categories;
        this.chefCertifications = chefCertifications;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
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
    
    public String getChefCoverImg() {
        return chefCoverImg;
    }
    
    public void setChefCoverImg(String chefCoverImg) {
        this.chefCoverImg = chefCoverImg;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
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
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Integer getTotalReviews() {
        return totalReviews;
    }
    
    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Double getDistanceKm() {
        return distanceKm;
    }
    
    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}