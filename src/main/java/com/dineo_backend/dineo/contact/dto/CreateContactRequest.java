package com.dineo_backend.dineo.contact.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for creating a contact message
 */
public class CreateContactRequest {

    @NotBlank(message = "Le nom est requis")
    @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères")
    private String fullName;

    @NotBlank(message = "L'email est requis")
    @Email(message = "L'email doit être valide")
    @Size(max = 255, message = "L'email ne peut pas dépasser 255 caractères")
    private String email;

    @Size(max = 200, message = "Le sujet ne peut pas dépasser 200 caractères")
    private String subject;

    @NotBlank(message = "Le message est requis")
    @Size(min = 10, max = 5000, message = "Le message doit contenir entre 10 et 5000 caractères")
    private String message;

    @Size(max = 30, message = "Le téléphone ne peut pas dépasser 30 caractères")
    private String phone;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
