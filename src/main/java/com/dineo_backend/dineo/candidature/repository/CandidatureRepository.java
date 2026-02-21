package com.dineo_backend.dineo.candidature.repository;

import com.dineo_backend.dineo.candidature.entity.Candidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repository for Candidature entity
 */
@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, UUID>, JpaSpecificationExecutor<Candidature> {
    
    /**
     * Find candidatures by viewed status
     */
    Page<Candidature> findByIsViewed(Boolean isViewed, Pageable pageable);
    
    /**
     * Find candidatures created between dates
     */
    Page<Candidature> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Count unviewed candidatures
     */
    long countByIsViewed(Boolean isViewed);
}
