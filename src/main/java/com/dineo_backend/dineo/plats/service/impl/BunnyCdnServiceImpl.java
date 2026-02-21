package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.plats.service.BunnyCdnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * Implementation of BunnyCdnService
 * Handles file uploads to Bunny CDN storage
 */
@Service
public class BunnyCdnServiceImpl implements BunnyCdnService {

    private static final Logger logger = LoggerFactory.getLogger(BunnyCdnServiceImpl.class);

    @Value("${bunny.api.url}")
    private String bunnyApiUrl;

    @Value("${bunny.api.key}")
    private String bunnyApiKey;

    @Value("${bunny.cdn.url}")
    private String bunnyCdnUrl;

    @Override
    public String uploadImage(MultipartFile file, String folder) {
        try {
            logger.info("Starting image upload to Bunny CDN. File: {}, Size: {} bytes", 
                       file.getOriginalFilename(), file.getSize());

            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Le fichier est vide");
            }

            // Validate file type
            if (!isImageFile(file)) {
                throw new RuntimeException("Le fichier doit être une image (PNG, JPG, JPEG, GIF, WEBP)");
            }

            // Generate unique filename
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            String fileExtension = getFileExtension(file);
            String finalFilename = uniqueFilename + "." + fileExtension;

            // Construct URLs
            String uploadUrl = bunnyApiUrl + "/" + folder + "/" + finalFilename;
            String cdnUrl = bunnyCdnUrl + "/" + folder + "/" + finalFilename;

            logger.info("Upload URL: {}", uploadUrl);
            logger.info("CDN URL: {}", cdnUrl);

            // Create connection
            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request properties
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("AccessKey", bunnyApiKey);
            connection.setRequestProperty("Content-Type", file.getContentType());
            connection.setRequestProperty("Content-Disposition", "filename=" + finalFilename);
            connection.setDoOutput(true);

            // Upload file
            try (BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {
                outputStream.write(file.getBytes());
                outputStream.flush();
            }

            // Check response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            logger.info("Upload response: {} - {}", responseCode, responseMessage);

            if (responseCode >= 200 && responseCode < 300) {
                logger.info("Image uploaded successfully to: {}", cdnUrl);
                return cdnUrl;
            } else {
                logger.error("Upload failed with response code: {} - {}", responseCode, responseMessage);
                throw new RuntimeException("Échec du téléchargement de l'image: " + responseMessage);
            }

        } catch (IOException e) {
            logger.error("IO error during file upload: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du téléchargement: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage());
            throw new RuntimeException("Erreur inattendue lors du téléchargement: " + e.getMessage());
        }
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        String baseName = originalFilename != null ? 
            originalFilename.replaceFirst("[.][^.]+$", "") : "file";
        
        // Clean base name (remove special characters)
        baseName = baseName.replaceAll("[^a-zA-Z0-9_-]", "_");
        
        return UUID.randomUUID().toString() + "_" + baseName;
    }

    @Override
    public String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        
        // Fallback to content type
        String contentType = file.getContentType();
        if (contentType != null) {
            switch (contentType) {
                case "image/jpeg":
                    return "jpg";
                case "image/png":
                    return "png";
                case "image/gif":
                    return "gif";
                case "image/webp":
                    return "webp";
                default:
                    return "jpg"; // Default fallback
            }
        }
        
        return "jpg"; // Default fallback
    }

    @Override
    public String uploadPdf(MultipartFile file, String folder) {
        try {
            logger.info("Starting PDF upload to Bunny CDN. File: {}, Size: {} bytes", 
                       file.getOriginalFilename(), file.getSize());

            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("Le fichier est vide");
            }

            // Validate file type
            if (!isPdfFile(file)) {
                throw new RuntimeException("Le fichier doit être un PDF");
            }

            // Generate unique filename
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            String finalFilename = uniqueFilename + ".pdf";

            // Construct URLs
            String uploadUrl = bunnyApiUrl + "/" + folder + "/" + finalFilename;
            String cdnUrl = bunnyCdnUrl + "/" + folder + "/" + finalFilename;

            logger.info("Upload URL: {}", uploadUrl);
            logger.info("CDN URL: {}", cdnUrl);

            // Create connection
            URL url = new URL(uploadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request properties
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("AccessKey", bunnyApiKey);
            connection.setRequestProperty("Content-Type", "application/pdf");
            connection.setRequestProperty("Content-Disposition", "filename=" + finalFilename);
            connection.setDoOutput(true);

            // Upload file
            try (BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {
                outputStream.write(file.getBytes());
                outputStream.flush();
            }

            // Check response
            int responseCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();

            logger.info("Upload response: {} - {}", responseCode, responseMessage);

            if (responseCode >= 200 && responseCode < 300) {
                logger.info("PDF uploaded successfully to: {}", cdnUrl);
                return cdnUrl;
            } else {
                logger.error("Upload failed with response code: {} - {}", responseCode, responseMessage);
                throw new RuntimeException("Échec du téléchargement du PDF: " + responseMessage);
            }

        } catch (IOException e) {
            logger.error("IO error during file upload: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du téléchargement: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage());
            throw new RuntimeException("Erreur inattendue lors du téléchargement: " + e.getMessage());
        }
    }

    /**
     * Check if uploaded file is a valid image
     * 
     * @param file the multipart file to check
     * @return true if file is an image, false otherwise
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.startsWith("image/") && 
               (contentType.equals("image/jpeg") || 
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp"));
    }
    
    /**
     * Check if uploaded file is a valid PDF
     * 
     * @param file the multipart file to check
     * @return true if file is a PDF, false otherwise
     */
    private boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("application/pdf");
    }
}