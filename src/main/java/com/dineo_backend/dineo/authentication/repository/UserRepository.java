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
     * Find user by phone number
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * Find user by email or phone number
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.phone = :identifier")
    Optional<User> findByEmailOrPhone(@Param("identifier") String identifier);
    
    /**
     * Check if user exists by email
     */
    Boolean existsByEmail(String email);
    
    /**
     * Check if user exists by phone number
     */
    Boolean existsByPhone(String phone);
    
    /**
     * Check if user exists by email or phone number
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :identifier OR u.phone = :identifier")
    Boolean existsByEmailOrPhone(@Param("identifier") String identifier);
    
    /**
     * Find users by first name or last name containing the search term (case insensitive)
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);
}
