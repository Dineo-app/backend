package com.dineo_backend.dineo.plats.controller;

import com.dineo_backend.dineo.plats.dto.IngredientRequest;
import com.dineo_backend.dineo.plats.dto.IngredientResponse;
import com.dineo_backend.dineo.plats.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing ingredients
 */
@RestController
@RequestMapping("/api/v1/plats/{platId}/ingredients")
@CrossOrigin(origins = "*")
public class IngredientController {

    private final IngredientService ingredientService;

    @Autowired
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /**
     * Create a new ingredient for a plate
     * Only accessible by CHEF role
     */
    @PostMapping
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<IngredientResponse> createIngredient(
            @PathVariable UUID platId,
            @Valid @RequestBody IngredientRequest request) {
        IngredientResponse response = ingredientService.createIngredient(platId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all ingredients for a plate
     * Public endpoint - no authentication required
     * Returns empty list if no ingredients found
     */
    @GetMapping
    public ResponseEntity<List<IngredientResponse>> getIngredientsByPlatId(@PathVariable UUID platId) {
        try {
            List<IngredientResponse> ingredients = ingredientService.getIngredientsByPlatId(platId);
            return ResponseEntity.ok(ingredients);
        } catch (Exception e) {
            // Return empty list if plate not found or has no ingredients
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Update an ingredient
     * Only accessible by CHEF role
     */
    @PutMapping("/{ingredientId}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<IngredientResponse> updateIngredient(
            @PathVariable UUID platId,
            @PathVariable UUID ingredientId,
            @Valid @RequestBody IngredientRequest request) {
        IngredientResponse response = ingredientService.updateIngredient(ingredientId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an ingredient
     * Only accessible by CHEF role
     */
    @DeleteMapping("/{ingredientId}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<Void> deleteIngredient(
            @PathVariable UUID platId,
            @PathVariable UUID ingredientId) {
        ingredientService.deleteIngredient(ingredientId);
        return ResponseEntity.noContent().build();
    }
}
