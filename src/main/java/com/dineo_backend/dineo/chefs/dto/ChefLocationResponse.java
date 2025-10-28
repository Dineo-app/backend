package com.dineo_backend.dineo.chefs.dto;

public class ChefLocationResponse {
    private String chefId;
    private String firstName;
    private String lastName;
    private String address;
    private String coverImageUrl;
    private Double latitude;
    private Double longitude;
    private Double distance; // in kilometers
    private Double averageRating;
    private Integer totalReviews;
    private String[] categories;

    // Constructors
    public ChefLocationResponse() {
    }

    public ChefLocationResponse(String chefId, String firstName, String lastName, 
                               String address, String coverImageUrl,
                               Double latitude, Double longitude) {
        this.chefId = chefId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.coverImageUrl = coverImageUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getChefId() {
        return chefId;
    }

    public void setChefId(String chefId) {
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
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

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }
}
