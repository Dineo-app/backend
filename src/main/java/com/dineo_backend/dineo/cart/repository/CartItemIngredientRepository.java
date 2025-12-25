package com.dineo_backend.dineo.cart.repository;

import com.dineo_backend.dineo.cart.model.CartItemIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartItemIngredientRepository extends JpaRepository<CartItemIngredient, UUID> {
    
    List<CartItemIngredient> findByCartItemId(UUID cartItemId);
    
    void deleteByCartItemId(UUID cartItemId);
}
