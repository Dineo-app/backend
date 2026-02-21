package com.dineo_backend.dineo.plats.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for Bunny CDN operations
 * Handles file upload to Bunny CDN storage
 */
public interface BunnyCdnService {
    
    /**
     * Upload image to Bunny CDN storage
     * 
     * @param file the image file to upload
     * @param folder the destination folder in storage (e.g., "plats")
     * @return the CDN URL of the uploaded image
     * @throws RuntimeException if upload fails
     */
    String uploadImage(MultipartFile file, String folder);
    
    /**
     * Upload PDF to Bunny CDN storage
     * 
     * @param file the PDF file to upload
     * @param folder the destination folder in storage (e.g., "candidatures")
     * @return the CDN URL of the uploaded PDF
     * @throws RuntimeException if upload fails
     */
    String uploadPdf(MultipartFile file, String folder);
    
    /**
     * Generate unique filename for uploaded file
     * 
     * @param originalFilename the original filename
     * @return unique filename with UUID prefix
     */
    String generateUniqueFilename(String originalFilename);
    
    /**
     * Get file extension from multipart file
     * 
     * @param file the multipart file
     * @return file extension (e.g., "jpg", "png", "pdf")
     */
    String getFileExtension(MultipartFile file);
}