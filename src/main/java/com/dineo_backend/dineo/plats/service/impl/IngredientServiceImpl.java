package com.dineo_backend.dineo.plats.service.impl;

import com.dineo_backend.dineo.plats.dto.IngredientRequest;
import com.dineo_backend.dineo.plats.dto.IngredientResponse;
import com.dineo_backend.dineo.plats.model.Ingredient;
import com.dineo_backend.dineo.plats.repository.IngredientRepository;
import com.dineo_backend.dineo.plats.repository.PlatRepository;
import com.dineo_backend.dineo.plats.service.IngredientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for managing ingredients
 */
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;
    private final PlatRepository platRepository;

    @Autowired
    public IngredientServiceImpl(IngredientRepository ingredientRepository, PlatRepository platRepository) {
        this.ingredientRepository = ingredientRepository;
        this.platRepository = platRepository;
    }

    @Override
    @Transactional
    public IngredientResponse createIngredient(UUID platId, IngredientRequest request) {
        // Verify plate exists
        if (!platRepository.existsById(platId)) {
            throw new RuntimeException("Plate not found with id: " + platId);
        }

        Ingredient ingredient = new Ingredient();
        ingredient.setPlatId(platId);
        ingredient.setName(request.getName());
        ingredient.setPrice(request.getPrice());
        ingredient.setIsFree(request.getIsFree());

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return mapToResponse(savedIngredient);
    }

    @Override
    public List<IngredientResponse> getIngredientsByPlatId(UUID platId) {
        List<Ingredient> ingredients = ingredientRepository.findByPlatId(platId);
        return ingredients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IngredientResponse updateIngredient(UUID ingredientId, IngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new RuntimeException("Ingredient not found with id: " + ingredientId));

        ingredient.setName(request.getName());
        ingredient.setPrice(request.getPrice());
        ingredient.setIsFree(request.getIsFree());

        Ingredient updatedIngredient = ingredientRepository.save(ingredient);
        return mapToResponse(updatedIngredient);
    }

    @Override
    @Transactional
    public void deleteIngredient(UUID ingredientId) {
        if (!ingredientRepository.existsById(ingredientId)) {
            throw new RuntimeException("Ingredient not found with id: " + ingredientId);
        }
        ingredientRepository.deleteById(ingredientId);
    }

    @Override
    @Transactional
    public void deleteIngredientsByPlatId(UUID platId) {
        ingredientRepository.deleteByPlatId(platId);
    }

    /**
     * Map Ingredient entity to IngredientResponse DTO
     */
    private IngredientResponse mapToResponse(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getPlatId(),
                ingredient.getName(),
                ingredient.getPrice(),
                ingredient.getIsFree()
        );
    }
}
