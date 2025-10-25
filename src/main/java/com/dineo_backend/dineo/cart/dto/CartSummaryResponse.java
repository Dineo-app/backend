package com.dineo_backend.dineo.cart.dto;

import java.util.List;

/**
 * DTO for cart summary response
 * Includes all cart items and total information
 */
public class CartSummaryResponse {

    private List<CartItemResponse> items;
    private Integer totalItems;
    private Double subtotal;
    private Double total;

    // Constructors
    public CartSummaryResponse() {
    }

    public CartSummaryResponse(List<CartItemResponse> items, Integer totalItems, Double subtotal, Double total) {
        this.items = items;
        this.totalItems = totalItems;
        this.subtotal = subtotal;
        this.total = total;
    }

    // Getters and Setters
    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}
