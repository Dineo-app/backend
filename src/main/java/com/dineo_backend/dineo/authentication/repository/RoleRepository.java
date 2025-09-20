package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<UserRole, UUID> {
    
    /**
     * Find role by name
     */
    Optional<UserRole> findByName(String name);
    
    /**
     * Check if role exists by name
     */
    Boolean existsByName(String name);
    
    /**
     * Find roles by name containing (case insensitive)
     */
    @Query("SELECT r FROM UserRole r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserRole> findByNameContaining(@Param("searchTerm") String searchTerm);
    
    /**
     * Find all roles ordered by name
     */
    @Query("SELECT r FROM UserRole r ORDER BY r.name ASC")
    List<UserRole> findAllOrderByName();
}
