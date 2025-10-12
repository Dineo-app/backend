package com.dineo_backend.dineo.chefs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for deleting chef certification image response
 */
public class DeleteCertificationResponse {
    
    @JsonProperty("chefId")
    private String chefId;
    
    @JsonProperty("deletedCertificationUrl")
    private String deletedCertificationUrl;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public DeleteCertificationResponse() {
    }
    
    // Constructor with all fields
    public DeleteCertificationResponse(String chefId, String deletedCertificationUrl, String message) {
        this.chefId = chefId;
        this.deletedCertificationUrl = deletedCertificationUrl;
        this.message = message;
    }
    
    // Getters and Setters
    public String getChefId() {
        return chefId;
    }
    
    public void setChefId(String chefId) {
        this.chefId = chefId;
    }
    
    public String getDeletedCertificationUrl() {
        return deletedCertificationUrl;
    }
    
    public void setDeletedCertificationUrl(String deletedCertificationUrl) {
        this.deletedCertificationUrl = deletedCertificationUrl;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "DeleteCertificationResponse{" +
                "chefId='" + chefId + '\'' +
                ", deletedCertificationUrl='" + deletedCertificationUrl + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}