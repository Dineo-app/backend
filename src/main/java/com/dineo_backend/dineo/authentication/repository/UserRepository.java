package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
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
     * Find users by role
     */
    List<User> findByRole(UserRole role);
    
    /**
     * Find verified users
     */
    List<User> findByIsVerified(Boolean isVerified);
    
    /**
     * Find active users
     */
    List<User> findByIsActive(Boolean isActive);
    
    /**
     * Find users by role and verification status
     */
    List<User> findByRoleAndIsVerified(UserRole role, Boolean isVerified);
    
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
     * Find users who have a specific role by role name
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
    
    /**
     * Find users who have a specific role and are verified
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.isVerified = :isVerified")
    List<User> findByRoleNameAndIsVerified(@Param("roleName") String roleName, @Param("isVerified") Boolean isVerified);
    
    /**
     * Find users who have a specific role and are active
     */
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.isActive = :isActive")
    List<User> findByRoleNameAndIsActive(@Param("roleName") String roleName, @Param("isActive") Boolean isActive);
    
    /**
     * Count users by role
     */
    long countByRole(UserRole role);
    
    /**
     * Count users by verification status
     */
    long countByIsVerified(Boolean isVerified);
    
    /**
     * Count users by active status
     */
    long countByIsActive(Boolean isActive);
    
    /**
     * Count users who have a specific role by role name
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);
}
