package com.dineo_backend.dineo.chefs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for updating chef cover image response
 */
public class UpdateChefCoverImageResponse {
    
    @JsonProperty("chefId")
    private String chefId;
    
    @JsonProperty("coverImageUrl")
    private String coverImageUrl;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public UpdateChefCoverImageResponse() {
    }
    
    // Constructor with all fields
    public UpdateChefCoverImageResponse(String chefId, String coverImageUrl, String message) {
        this.chefId = chefId;
        this.coverImageUrl = coverImageUrl;
        this.message = message;
    }
    
    // Getters and Setters
    public String getChefId() {
        return chefId;
    }
    
    public void setChefId(String chefId) {
        this.chefId = chefId;
    }
    
    public String getCoverImageUrl() {
        return coverImageUrl;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "UpdateChefCoverImageResponse{" +
                "chefId='" + chefId + '\'' +
                ", coverImageUrl='" + coverImageUrl + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}