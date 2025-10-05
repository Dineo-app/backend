package com.dineo_backend.dineo.administration.controller;

import com.dineo_backend.dineo.authentication.enums.Role;
import com.dineo_backend.dineo.authentication.model.User;
import com.dineo_backend.dineo.authentication.model.UserRole;
import com.dineo_backend.dineo.authentication.repository.RoleRepository;
import com.dineo_backend.dineo.authentication.repository.UserRepository;
import com.dineo_backend.dineo.shared.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * TEMPORARY Controller for promoting users to admin
 * DELETE THIS IN PRODUCTION!
 */
@RestController
@RequestMapping("/api/temp")
@CrossOrigin(origins = "*")
public class PromoteController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * TEMPORARY: Promote user to admin by email
     * DELETE THIS ENDPOINT IN PRODUCTION!
     */
    @PostMapping("/promote-to-admin/{email}")
    public ResponseEntity<ApiResponse<String>> promoteToAdmin(@PathVariable String email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "User not found"));
            }

            User user = userOpt.get();
            
            // Check if already admin
            Optional<UserRole> existingAdmin = roleRepository.findByUserIdAndRole(user.getId(), Role.ADMIN);
            if (existingAdmin.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("User is already an admin", "Already admin"));
            }

            // Add admin role
            UserRole adminRole = new UserRole(user.getId(), Role.ADMIN);
            roleRepository.save(adminRole);

            return ResponseEntity.ok(ApiResponse.success("User promoted to admin successfully", 
                "User " + email + " is now an admin"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, "Error: " + e.getMessage()));
        }
    }
}