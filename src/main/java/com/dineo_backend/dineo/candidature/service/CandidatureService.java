package com.dineo_backend.dineo.candidature.service;

import com.dineo_backend.dineo.candidature.dto.CandidatureResponse;
import com.dineo_backend.dineo.candidature.dto.CreateCandidatureRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for Candidature operations
 */
public interface CandidatureService {
    
    /**
     * Create a new candidature (public endpoint)
     * 
     * @param request the candidature data
     * @param resumeFile the PDF resume file
     * @return the created candidature
     */
    CandidatureResponse createCandidature(CreateCandidatureRequest request, MultipartFile resumeFile);
    
    /**
     * Get all candidatures with pagination and filters (admin only)
     * 
     * @param isViewed filter by viewed status (optional)
     * @param startDate filter by start date (optional)
     * @param endDate filter by end date (optional)
     * @param country filter by country (optional)
     * @param pageable pagination parameters
     * @return page of candidatures
     */
    Page<CandidatureResponse> getAllCandidatures(Boolean isViewed, LocalDate startDate, LocalDate endDate, 
                                                  String country, Pageable pageable);
    
    /**
     * Get candidature by ID (admin only) - marks as viewed
     * 
     * @param id the candidature ID
     * @return the candidature
     */
    CandidatureResponse getCandidatureById(UUID id);
    
    /**
     * Get count of unviewed candidatures
     * 
     * @return count of unviewed candidatures
     */
    long getUnviewedCount();
}
