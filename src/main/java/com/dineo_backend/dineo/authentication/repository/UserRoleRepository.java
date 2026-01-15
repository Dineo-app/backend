package com.dineo_backend.dineo.authentication.repository;

import com.dineo_backend.dineo.authentication.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {
    
    Optional<UserRole> findByUserId(UUID userId);
}
