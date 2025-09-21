package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by email
     */
    Boolean existsByEmail(String email);
    
    /**
     * Find verified users
     */
    List<User> findByIsVerified(Boolean isVerified);
    
    /**
     * Find active users
     */
    List<User> findByIsActive(Boolean isActive);
    
    /**
     * Find user by email and verification status
     */
    Optional<User> findByEmailAndIsVerified(String email, Boolean isVerified);
    
    /**
     * Find user by email and active status
     */
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);
    
    /**
     * Find users by first name or last name containing the search term (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Count users by verification status
     */
    long countByIsVerified(Boolean isVerified);
    
    /**
     * Count users by active status
     */
    long countByIsActive(Boolean isActive);
}
