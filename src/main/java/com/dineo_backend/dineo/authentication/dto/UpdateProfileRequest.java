package com.dineo_backend.dineo.authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user profile information.
 */
public class UpdateProfileRequest {
    
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
    @JsonProperty("first_name")
    private String firstName;
    
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
    @JsonProperty("last_name")
    private String lastName;
    
    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne doit pas dépasser 100 caractères")
    private String email;
    
    @Size(max = 15, message = "Le numéro de téléphone ne doit pas dépasser 15 caractères")
    private String phone;
    
    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String address;
    
    // Default constructor
    public UpdateProfileRequest() {
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
}
