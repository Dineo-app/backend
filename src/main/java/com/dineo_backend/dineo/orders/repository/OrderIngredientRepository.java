package com.dineo_backend.dineo.orders.repository;

import com.dineo_backend.dineo.orders.model.OrderIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing order ingredients
 */
@Repository
public interface OrderIngredientRepository extends JpaRepository<OrderIngredient, UUID> {
    
    /**
     * Find all ingredients for a specific order
     * @param orderId the ID of the order
     * @return list of ingredients for the order
     */
    List<OrderIngredient> findByOrderId(UUID orderId);
}
