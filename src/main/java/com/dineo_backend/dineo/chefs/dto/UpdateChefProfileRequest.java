package com.dineo_backend.dineo.chefs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for updating chef profile information
 * Includes both user and chef description fields
 */
public class UpdateChefProfileRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    @Email(message = "Format d'email invalide")
    private String email;

    @Size(max = 15, message = "Le téléphone ne peut pas dépasser 15 caractères")
    private String phone;

    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    private String address;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    private String description;

    private List<String> categories;

    // Default constructor
    public UpdateChefProfileRequest() {}

    // Constructor with all fields
    public UpdateChefProfileRequest(String firstName, String lastName, String email, 
                                  String phone, String address, String description, 
                                  List<String> categories) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.description = description;
        this.categories = categories;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "UpdateChefProfileRequest{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", categories=" + categories +
                '}';
    }
}