package com.dineo_backend.dineo.candidature.service.impl;

import com.dineo_backend.dineo.candidature.dto.CandidatureResponse;
import com.dineo_backend.dineo.candidature.dto.CreateCandidatureRequest;
import com.dineo_backend.dineo.candidature.entity.Candidature;
import com.dineo_backend.dineo.candidature.repository.CandidatureRepository;
import com.dineo_backend.dineo.candidature.service.CandidatureService;
import com.dineo_backend.dineo.plats.service.BunnyCdnService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of CandidatureService
 */
@Service
public class CandidatureServiceImpl implements CandidatureService {
    
    private static final Logger logger = LoggerFactory.getLogger(CandidatureServiceImpl.class);
    
    private final CandidatureRepository candidatureRepository;
    private final BunnyCdnService bunnyCdnService;
    
    public CandidatureServiceImpl(CandidatureRepository candidatureRepository, 
                                  BunnyCdnService bunnyCdnService) {
        this.candidatureRepository = candidatureRepository;
        this.bunnyCdnService = bunnyCdnService;
    }
    
    @Override
    @Transactional
    public CandidatureResponse createCandidature(CreateCandidatureRequest request, MultipartFile resumeFile) {
        logger.info("Creating new candidature for: {} {}", request.getFirstName(), request.getLastName());
        
        // Upload resume PDF to BunnyCDN
        String resumeUrl;
        try {
            resumeUrl = bunnyCdnService.uploadPdf(resumeFile, "candidatures");
            logger.info("Resume uploaded successfully: {}", resumeUrl);
        } catch (Exception e) {
            logger.error("Failed to upload resume: {}", e.getMessage());
            throw new RuntimeException("Erreur lors du téléchargement du CV");
        }
        
        // Create candidature entity
        Candidature candidature = new Candidature();
        candidature.setFirstName(request.getFirstName());
        candidature.setLastName(request.getLastName());
        candidature.setEmail(request.getEmail());
        candidature.setPhone(request.getPhone());
        candidature.setDateOfBirth(request.getDateOfBirth());
        candidature.setDescription(request.getDescription());
        candidature.setCountry(request.getCountry());
        candidature.setResumeUrl(resumeUrl);
        candidature.setIsViewed(false);
        
        // Save to database
        Candidature savedCandidature = candidatureRepository.save(candidature);
        logger.info("Candidature created successfully with ID: {}", savedCandidature.getId());
        
        return mapToResponse(savedCandidature);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<CandidatureResponse> getAllCandidatures(Boolean isViewed, LocalDate startDate, LocalDate endDate, 
                                                        String country, Pageable pageable) {
        logger.info("Fetching candidatures with filters - isViewed: {}, startDate: {}, endDate: {}, country: {}", 
                   isViewed, startDate, endDate, country);
        
        Specification<Candidature> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (isViewed != null) {
                predicates.add(criteriaBuilder.equal(root.get("isViewed"), isViewed));
            }
            
            if (startDate != null) {
                LocalDateTime startDateTime = startDate.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            
            if (endDate != null) {
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }
            
            if (country != null && !country.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("country"), country));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Candidature> candidatures = candidatureRepository.findAll(spec, pageable);
        logger.info("Found {} candidatures", candidatures.getTotalElements());
        
        return candidatures.map(this::mapToResponse);
    }
    
    @Override
    @Transactional
    public CandidatureResponse getCandidatureById(UUID id) {
        logger.info("Fetching candidature with ID: {}", id);
        
        Candidature candidature = candidatureRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        
        // Mark as viewed if not already
        if (!candidature.getIsViewed()) {
            candidature.setIsViewed(true);
            candidatureRepository.save(candidature);
            logger.info("Candidature {} marked as viewed", id);
        }
        
        return mapToResponse(candidature);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getUnviewedCount() {
        return candidatureRepository.countByIsViewed(false);
    }
    
    /**
     * Map Candidature entity to response DTO
     */
    private CandidatureResponse mapToResponse(Candidature candidature) {
        CandidatureResponse response = new CandidatureResponse();
        response.setId(candidature.getId());
        response.setFirstName(candidature.getFirstName());
        response.setLastName(candidature.getLastName());
        response.setEmail(candidature.getEmail());
        response.setPhone(candidature.getPhone());
        response.setDateOfBirth(candidature.getDateOfBirth());
        response.setDescription(candidature.getDescription());
        response.setCountry(candidature.getCountry());
        response.setResumeUrl(candidature.getResumeUrl());
        response.setIsViewed(candidature.getIsViewed());
        response.setCreatedAt(candidature.getCreatedAt());
        response.setUpdatedAt(candidature.getUpdatedAt());
        return response;
    }
}
