package com.dineo_backend.dineo.chefs.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for retrieving chef profile information
 * Contains all chef profile data including cover image and certifications
 */
public class GetChefProfileResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String description;
    private List<String> categories;
    private String coverImageUrl;
    private List<String> certificationImageUrls;

    // Default constructor
    public GetChefProfileResponse() {}

    // Constructor with all fields
    public GetChefProfileResponse(UUID userId, String firstName, String lastName, String email,
                                 String phone, String address, String description,
                                 List<String> categories, String coverImageUrl,
                                 List<String> certificationImageUrls) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.description = description;
        this.categories = categories;
        this.coverImageUrl = coverImageUrl;
        this.certificationImageUrls = certificationImageUrls;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public List<String> getCertificationImageUrls() {
        return certificationImageUrls;
    }

    public void setCertificationImageUrls(List<String> certificationImageUrls) {
        this.certificationImageUrls = certificationImageUrls;
    }

    @Override
    public String toString() {
        return "GetChefProfileResponse{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", categories=" + categories +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", certificationImageUrls=" + certificationImageUrls +
                '}';
    }
}
