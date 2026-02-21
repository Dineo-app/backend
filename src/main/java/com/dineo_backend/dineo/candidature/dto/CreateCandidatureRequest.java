package com.dineo_backend.dineo.candidature.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * DTO for creating a new candidature
 */
public class CreateCandidatureRequest {
    
    @NotBlank(message = "Le prénom est requis")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String firstName;
    
    @NotBlank(message = "Le nom est requis")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String lastName;
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    private String email;
    
    @NotBlank(message = "Le téléphone est requis")
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    private String phone;
    
    @NotNull(message = "La date de naissance est requise")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;
    
    @NotBlank(message = "La description est requise")
    @Size(min = 50, max = 2000, message = "La description doit contenir entre 50 et 2000 caractères")
    private String description;
    
    @NotBlank(message = "Le pays est requis")
    @Size(max = 100, message = "Le pays ne peut pas dépasser 100 caractères")
    private String country;
    
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
    
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
}
