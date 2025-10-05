package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, UUID> {
    
    /**
     * Find UserRole by userId and role
     */
    Optional<UserRole> findByUserIdAndRole(UUID userId, Role role);
    
    /**
     * Find all UserRoles by userId
     */
    List<UserRole> findByUserId(UUID userId);
    
    /**
     * Check if user has specific role
     */
    Boolean existsByUserIdAndRole(UUID userId, Role role);
    
    /**
     * Find all UserRoles by role
     */
    List<UserRole> findByRole(Role role);
    
    /**
     * Delete UserRole by userId and role
     */
    void deleteByUserIdAndRole(UUID userId, Role role);
    
    /**
     * Delete all UserRoles for a user
     */
    void deleteByUserId(UUID userId);
}
