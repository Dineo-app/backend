package com.dineo_backend.dineo.chefs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for uploading chef certification image response
 */
public class UploadCertificationResponse {
    
    @JsonProperty("chefId")
    private String chefId;
    
    @JsonProperty("certificationImageUrl")
    private String certificationImageUrl;
    
    @JsonProperty("message")
    private String message;
    
    // Default constructor
    public UploadCertificationResponse() {
    }
    
    // Constructor with all fields
    public UploadCertificationResponse(String chefId, String certificationImageUrl, String message) {
        this.chefId = chefId;
        this.certificationImageUrl = certificationImageUrl;
        this.message = message;
    }
    
    // Getters and Setters
    public String getChefId() {
        return chefId;
    }
    
    public void setChefId(String chefId) {
        this.chefId = chefId;
    }
    
    public String getCertificationImageUrl() {
        return certificationImageUrl;
    }
    
    public void setCertificationImageUrl(String certificationImageUrl) {
        this.certificationImageUrl = certificationImageUrl;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "UploadCertificationResponse{" +
                "chefId='" + chefId + '\'' +
                ", certificationImageUrl='" + certificationImageUrl + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}