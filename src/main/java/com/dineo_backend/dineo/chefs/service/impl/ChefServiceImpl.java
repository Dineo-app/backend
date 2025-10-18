package com.dineo_backend.dineo.chefs.service.impl;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.chefs.dto.DeleteCertificationResponse;
import com.dineo_backend.dineo.chefs.dto.GetChefProfileResponse;
import com.dineo_backend.dineo.chefs.dto.UpdateChefCoverImageResponse;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileRequest;
import com.dineo_backend.dineo.chefs.dto.UpdateChefProfileResponse;
import com.dineo_backend.dineo.chefs.dto.UploadCertificationResponse;
import com.dineo_backend.dineo.chefs.model.ChefDescription;
import com.dineo_backend.dineo.chefs.repository.ChefDescriptionRepository;
import com.dineo_backend.dineo.chefs.service.ChefService;
import com.dineo_backend.dineo.plats.service.BunnyCdnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of ChefService for chef-related operations
 */
@Service
public class ChefServiceImpl implements ChefService {

    private static final Logger logger = LoggerFactory.getLogger(ChefServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChefDescriptionRepository chefDescriptionRepository;

    @Autowired
    private BunnyCdnService bunnyCdnService;

    @Override
    @Transactional(readOnly = true)
    public GetChefProfileResponse getChefProfile(UUID chefUserId) {
        logger.info("Retrieving chef profile for user ID: {}", chefUserId);

        // Find user
        User user = userRepository.findById(chefUserId)
                .orElseThrow(() -> new RuntimeException("Chef utilisateur non trouvé avec l'ID: " + chefUserId));

        // Find chef description
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefUserId)
                .orElseThrow(() -> new RuntimeException("Description du chef non trouvée pour l'utilisateur ID: " + chefUserId));

        // Create and return response
        GetChefProfileResponse response = new GetChefProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                chefDescription.getDescription(),
                chefDescription.getCategories() != null ? chefDescription.getCategories() : new ArrayList<>(),
                chefDescription.getChefCoverImg(),
                chefDescription.getChefCertificationImages() != null ? chefDescription.getChefCertificationImages() : new ArrayList<>()
        );

        logger.info("Chef profile retrieved successfully for user ID: {}", chefUserId);
        return response;
    }

    @Override
    @Transactional
    public UpdateChefProfileResponse updateChefProfile(UUID chefUserId, UpdateChefProfileRequest request) {
        logger.info("Updating chef profile for user ID: {}", chefUserId);

        // Find and update user information
        User user = userRepository.findById(chefUserId)
                .orElseThrow(() -> new RuntimeException("Chef utilisateur non trouvé avec l'ID: " + chefUserId));

        // Check if email is already taken by another user
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
        }

        // Update user fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User updatedUser = userRepository.save(user);
        logger.info("User information updated successfully for user ID: {}", chefUserId);

        // Find and update chef description
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefUserId)
                .orElseThrow(() -> new RuntimeException("Description du chef non trouvée pour l'utilisateur ID: " + chefUserId));

        // Update chef description fields
        chefDescription.setDescription(request.getDescription());
        chefDescription.setCategories(request.getCategories());

        ChefDescription updatedChefDescription = chefDescriptionRepository.save(chefDescription);
        logger.info("Chef description updated successfully for user ID: {}", chefUserId);

        // Create and return response
        UpdateChefProfileResponse response = new UpdateChefProfileResponse(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getAddress(),
                updatedUser.getUpdatedAt(),
                updatedChefDescription.getId(),
                updatedChefDescription.getDescription(),
                updatedChefDescription.getCategories(),
                updatedChefDescription.getUpdatedAt()
        );

        logger.info("Chef profile update completed successfully for user ID: {}", chefUserId);
        return response;
    }

    @Override
    @Transactional
    public UpdateChefCoverImageResponse updateChefCoverImage(UUID chefUserId, MultipartFile imageFile) {
        logger.info("Updating chef cover image for user ID: {}", chefUserId);

        // Validate that the user exists and is a chef
        User user = userRepository.findById(chefUserId)
                .orElseThrow(() -> new RuntimeException("Chef utilisateur non trouvé avec l'ID: " + chefUserId));

        // Find chef description
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefUserId)
                .orElseThrow(() -> new RuntimeException("Description du chef non trouvée pour l'utilisateur ID: " + chefUserId));

        // Upload image to Bunny CDN
        String imageUrl = null;
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                logger.info("Uploading cover image for chef: {}", user.getFirstName() + " " + user.getLastName());
                imageUrl = bunnyCdnService.uploadImage(imageFile, "chef_cover_image");
                logger.info("Cover image uploaded successfully: {}", imageUrl);
            } else {
                throw new RuntimeException("Aucun fichier image fourni");
            }
        } catch (Exception e) {
            logger.error("Error uploading cover image for chef {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors du téléchargement de l'image: " + e.getMessage());
        }

        // Update chef description with new cover image URL
        chefDescription.setChefCoverImg(imageUrl);
        chefDescriptionRepository.save(chefDescription);

        logger.info("Chef cover image updated successfully for user ID: {}", chefUserId);

        // Create and return response
        UpdateChefCoverImageResponse response = new UpdateChefCoverImageResponse(
                chefUserId.toString(),
                imageUrl,
                "Image de couverture mise à jour avec succès"
        );

        return response;
    }

    @Override
    @Transactional
    public UploadCertificationResponse uploadCertificationImage(UUID chefUserId, MultipartFile certificationImage) {
        logger.info("Uploading certification image for chef user ID: {}", chefUserId);

        // Validate file
        if (certificationImage == null || certificationImage.isEmpty()) {
            throw new IllegalArgumentException("Le fichier de certification ne peut pas être vide");
        }

        // Check file type
        String contentType = certificationImage.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }

        // Check file size (5MB limit)
        if (certificationImage.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La taille du fichier ne doit pas dépasser 5MB");
        }

        // Find chef description
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefUserId)
                .orElseThrow(() -> new RuntimeException("Description du chef non trouvée"));

        try {
            // Upload to Bunny CDN
            String imageUrl = bunnyCdnService.uploadImage(certificationImage, "certifications");

            // Add to certification images list
            if (chefDescription.getChefCertificationImages() == null) {
                chefDescription.setChefCertificationImages(new ArrayList<>());
            }
            chefDescription.getChefCertificationImages().add(imageUrl);
            chefDescriptionRepository.save(chefDescription);

            logger.info("Certification image uploaded successfully for chef user ID: {}", chefUserId);

            // Create and return response
            return new UploadCertificationResponse(
                    chefUserId.toString(),
                    imageUrl,
                    "Image de certification ajoutée avec succès"
            );

        } catch (Exception e) {
            logger.error("Error uploading certification image for chef user ID {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors du téléchargement de l'image de certification: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DeleteCertificationResponse deleteCertificationImage(UUID chefUserId, String certificationImageUrl) {
        logger.info("Deleting certification image for chef user ID: {} with URL: {}", chefUserId, certificationImageUrl);

        // Validate URL
        if (certificationImageUrl == null || certificationImageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("L'URL de l'image de certification ne peut pas être vide");
        }

        // Find chef description
        ChefDescription chefDescription = chefDescriptionRepository.findByUserId(chefUserId)
                .orElseThrow(() -> new RuntimeException("Description du chef non trouvée"));

        // Check if certification images list exists and contains the URL
        List<String> certificationImages = chefDescription.getChefCertificationImages();
        if (certificationImages == null || certificationImages.isEmpty()) {
            throw new RuntimeException("Aucune image de certification trouvée");
        }

        if (!certificationImages.contains(certificationImageUrl)) {
            throw new RuntimeException("Image de certification non trouvée dans la liste");
        }

        try {
            // Note: For now, we only remove from database
            // Future enhancement: implement delete method in BunnyCdnService
            // bunnyCdnService.deleteImage(certificationImageUrl, "certifications");

            // Remove from certification images list
            certificationImages.remove(certificationImageUrl);
            chefDescriptionRepository.save(chefDescription);

            logger.info("Certification image deleted successfully for chef user ID: {}", chefUserId);

            // Create and return response
            return new DeleteCertificationResponse(
                    chefUserId.toString(),
                    certificationImageUrl,
                    "Image de certification supprimée avec succès"
            );

        } catch (Exception e) {
            logger.error("Error deleting certification image for chef user ID {}: {}", chefUserId, e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'image de certification: " + e.getMessage());
        }
    }
}